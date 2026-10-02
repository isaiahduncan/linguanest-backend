package com.linguanest.backend.chat;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.linguanest.backend.exercise.Exercise;
import com.linguanest.backend.exercise.ExerciseRepository;
import com.linguanest.backend.exercise.Question;
import com.linguanest.backend.exercise.QuestionRepository;
import com.linguanest.backend.submission.GradedResult;
import com.linguanest.backend.submission.GradedResultRepository;
import com.linguanest.backend.submission.Submission;
import com.linguanest.backend.submission.SubmissionRepository;
import com.linguanest.backend.user.PresumedUser;
import com.linguanest.backend.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final GradedResultRepository gradedResultRepository;

    @Transactional
    ChatSummary createChat(String title) {
        // A lazy proxy carrying just the id - avoids a round-trip to fetch the presumed user
        // just to reference it as the chat's owner.
        Chat chat = new Chat();
        chat.setUser(userRepository.getReferenceById(PresumedUser.ID));
        chat.setTitle(title);
        return ChatSummary.from(chatRepository.save(chat));
    }

    @Transactional(readOnly = true)
    List<ChatSummary> listChats() {
        return chatRepository.findByUserIdOrderByUpdatedAtDesc(PresumedUser.ID).stream()
                .map(ChatSummary::from)
                .toList();
    }

    @Transactional(readOnly = true)
    ChatMessagesResponse getMessages(UUID chatId) {
        // existsByIdAndUserId rather than findById: avoids hydrating the full Chat entity just
        // to check it exists, and ties the lookup to the presumed user - findById alone would
        // let any caller read any chat's history the moment a second user row ever exists.
        if (!chatRepository.existsByIdAndUserId(chatId, PresumedUser.ID)) {
            throw new ChatNotFoundException(chatId);
        }

        List<Exercise> exercises = exerciseRepository.findByChatIdOrderByCreatedAtAsc(chatId);
        if (exercises.isEmpty()) {
            return new ChatMessagesResponse(chatId, List.of());
        }
        List<UUID> exerciseIds = exercises.stream().map(Exercise::getId).toList();

        Map<UUID, List<QuestionDto>> questionsByExerciseId = questionRepository
                .findByExerciseIdInOrderByQuestionNumberAsc(exerciseIds).stream()
                .collect(Collectors.groupingBy(
                        question -> question.getExercise().getId(),
                        Collectors.mapping(ChatService::toQuestionDto, Collectors.toList())));

        List<Submission> submissions = submissionRepository.findByExerciseIdInOrderByCreatedAtAsc(exerciseIds);
        List<UUID> submissionIds = submissions.stream().map(Submission::getId).toList();

        // Merge function keeps the most recently created graded result when a submission has
        // more than one: graded_results.submission_id has no UNIQUE constraint (see
        // backend-api-spec.md Open Items), so a regraded submission has two rows, and
        // Collectors.toMap throws IllegalStateException on a duplicate key with no merge function -
        // every request for that chat's history would 500 instead of just this one entry.
        Map<UUID, GradedResultDto> gradedResultBySubmissionId = gradedResultRepository
                .findBySubmissionIdIn(submissionIds).stream()
                .collect(Collectors.toMap(
                        result -> result.getSubmission().getId(),
                        ChatService::toGradedResultDto,
                        (existing, candidate) -> candidate.createdAt().isAfter(existing.createdAt())
                                ? candidate
                                : existing));

        // exercises and submissions are each already sorted by createdAt ascending (the
        // repository methods above do that) - concat-then-sort is simpler to read than the two
        // separate accumulation loops this replaced, though it does mean the combined list gets
        // sorted again rather than merging the two already-sorted runs in O(n+m); at the data
        // volumes a single chat's history reaches, that's not worth the extra complexity of a
        // hand-rolled merge.
        // Explicit <ChatTimelineEntry> witness: without it, javac infers an intersection type
        // (Record & ChatTimelineEntry) from the two concrete record types instead of the sealed
        // interface itself, which doesn't assign to List<ChatTimelineEntry>.
        List<ChatTimelineEntry> entries = Stream.<ChatTimelineEntry>concat(
                        exercises.stream().map(exercise -> toExerciseTimelineEntry(exercise, questionsByExerciseId)),
                        submissions.stream()
                                .map(submission -> toSubmissionTimelineEntry(submission, gradedResultBySubmissionId)))
                .sorted(Comparator.comparing(ChatTimelineEntry::createdAt))
                .toList();

        return new ChatMessagesResponse(chatId, entries);
    }

    private static ExerciseTimelineEntry toExerciseTimelineEntry(
            Exercise exercise, Map<UUID, List<QuestionDto>> questionsByExerciseId) {
        return new ExerciseTimelineEntry(
                exercise.getId(),
                exercise.getLanguage(),
                exercise.getTopic(),
                exercise.getCreatedAt(),
                questionsByExerciseId.getOrDefault(exercise.getId(), List.of()));
    }

    private static SubmissionTimelineEntry toSubmissionTimelineEntry(
            Submission submission, Map<UUID, GradedResultDto> gradedResultBySubmissionId) {
        return new SubmissionTimelineEntry(
                submission.getId(),
                submission.getExercise().getId(),
                submission.getSubmissionType(),
                submission.getStatus(),
                submission.getRawTextInput(),
                submission.getCreatedAt(),
                gradedResultBySubmissionId.get(submission.getId()));
    }

    private static QuestionDto toQuestionDto(Question question) {
        return new QuestionDto(
                question.getId(), question.getQuestionNumber(), question.getPromptText(), question.getAnswerKey());
    }

    private static GradedResultDto toGradedResultDto(GradedResult result) {
        return new GradedResultDto(
                result.getId(),
                result.getOverallScore(),
                result.getFeedbackMarkdown(),
                result.getDetailedCorrections(),
                result.getCreatedAt());
    }
}
