package com.linguanest.backend.chat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
        chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException(chatId));

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

        Map<UUID, GradedResultDto> gradedResultBySubmissionId = submissionIds.isEmpty()
                ? Map.of()
                : gradedResultRepository.findBySubmissionIdIn(submissionIds).stream()
                        .collect(Collectors.toMap(
                                result -> result.getSubmission().getId(),
                                ChatService::toGradedResultDto));

        List<ChatTimelineEntry> entries = new ArrayList<>(exercises.size() + submissions.size());
        for (Exercise exercise : exercises) {
            entries.add(new ExerciseTimelineEntry(
                    exercise.getId(),
                    exercise.getLanguage(),
                    exercise.getTopic(),
                    exercise.getCreatedAt(),
                    questionsByExerciseId.getOrDefault(exercise.getId(), List.of())));
        }
        for (Submission submission : submissions) {
            entries.add(new SubmissionTimelineEntry(
                    submission.getId(),
                    submission.getExercise().getId(),
                    submission.getSubmissionType(),
                    submission.getStatus(),
                    submission.getRawTextInput(),
                    submission.getCreatedAt(),
                    gradedResultBySubmissionId.get(submission.getId())));
        }
        entries.sort(Comparator.comparing(ChatTimelineEntry::createdAt));

        return new ChatMessagesResponse(chatId, entries);
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
