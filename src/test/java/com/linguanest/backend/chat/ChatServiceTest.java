package com.linguanest.backend.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.linguanest.backend.exercise.Exercise;
import com.linguanest.backend.exercise.ExerciseRepository;
import com.linguanest.backend.exercise.Question;
import com.linguanest.backend.exercise.QuestionRepository;
import com.linguanest.backend.submission.GradedResult;
import com.linguanest.backend.submission.GradedResultRepository;
import com.linguanest.backend.submission.Submission;
import com.linguanest.backend.submission.SubmissionRepository;
import com.linguanest.backend.submission.SubmissionStatus;
import com.linguanest.backend.submission.SubmissionType;
import com.linguanest.backend.user.PresumedUser;
import com.linguanest.backend.user.User;
import com.linguanest.backend.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private GradedResultRepository gradedResultRepository;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(
                chatRepository, userRepository, exerciseRepository, questionRepository,
                submissionRepository, gradedResultRepository);
    }

    @Test
    void createChatSavesAChatOwnedByThePresumedUserAndReturnsItsSummary() {
        User presumedUserReference = new User();
        presumedUserReference.setId(PresumedUser.ID);
        when(userRepository.getReferenceById(PresumedUser.ID)).thenReturn(presumedUserReference);

        Chat savedChat = new Chat();
        savedChat.setId(UUID.randomUUID());
        savedChat.setTitle("Spanish practice");
        savedChat.setCreatedAt(Instant.now());
        savedChat.setUpdatedAt(Instant.now());
        when(chatRepository.save(any(Chat.class))).thenReturn(savedChat);

        ChatSummary result = chatService.createChat("Spanish practice");

        ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(presumedUserReference);
        assertThat(captor.getValue().getTitle()).isEqualTo("Spanish practice");

        assertThat(result.id()).isEqualTo(savedChat.getId());
        assertThat(result.title()).isEqualTo("Spanish practice");
    }

    @Test
    void listChatsDelegatesToTheRepositoryForThePresumedUser() {
        Chat chat = new Chat();
        chat.setId(UUID.randomUUID());
        chat.setTitle("Oldest first? No - newest updated first");
        chat.setCreatedAt(Instant.now());
        chat.setUpdatedAt(Instant.now());
        when(chatRepository.findByUserIdOrderByUpdatedAtDesc(PresumedUser.ID)).thenReturn(List.of(chat));

        List<ChatSummary> result = chatService.listChats();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(chat.getId());
    }

    @Test
    void getMessagesThrowsWhenTheChatDoesNotExist() {
        UUID missingChatId = UUID.randomUUID();
        when(chatRepository.findById(missingChatId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getMessages(missingChatId))
                .isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    void getMessagesReturnsAnEmptyTimelineWhenTheChatHasNoExercisesYet() {
        UUID chatId = UUID.randomUUID();
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(new Chat()));
        when(exerciseRepository.findByChatIdOrderByCreatedAtAsc(chatId)).thenReturn(List.of());

        ChatMessagesResponse result = chatService.getMessages(chatId);

        assertThat(result.chatId()).isEqualTo(chatId);
        assertThat(result.messages()).isEmpty();
    }

    @Test
    void getMessagesMergesExercisesQuestionsSubmissionsAndGradedResultsInChronologicalOrder() {
        UUID chatId = UUID.randomUUID();
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(new Chat()));

        Instant t0 = Instant.now().minus(10, ChronoUnit.MINUTES);

        Exercise exercise = new Exercise();
        exercise.setId(UUID.randomUUID());
        exercise.setLanguage("es");
        exercise.setTopic("past tense subjunctive");
        exercise.setCreatedAt(t0);
        when(exerciseRepository.findByChatIdOrderByCreatedAtAsc(chatId)).thenReturn(List.of(exercise));

        Question question = new Question();
        question.setId(UUID.randomUUID());
        question.setExercise(exercise);
        question.setQuestionNumber(1);
        question.setPromptText("Translate: 'I would have gone'");
        question.setAnswerKey("Hubiera ido");
        when(questionRepository.findByExerciseIdInOrderByQuestionNumberAsc(List.of(exercise.getId())))
                .thenReturn(List.of(question));

        Submission submission = new Submission();
        submission.setId(UUID.randomUUID());
        submission.setExercise(exercise);
        submission.setSubmissionType(SubmissionType.TEXT);
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setRawTextInput("Hubiera ido");
        submission.setCreatedAt(t0.plus(5, ChronoUnit.MINUTES));
        when(submissionRepository.findByExerciseIdInOrderByCreatedAtAsc(List.of(exercise.getId())))
                .thenReturn(List.of(submission));

        GradedResult gradedResult = new GradedResult();
        gradedResult.setId(UUID.randomUUID());
        gradedResult.setSubmission(submission);
        gradedResult.setOverallScore(100);
        gradedResult.setFeedbackMarkdown("Perfect!");
        gradedResult.setDetailedCorrections(Map.of("q1", "correct"));
        gradedResult.setCreatedAt(t0.plus(6, ChronoUnit.MINUTES));
        when(gradedResultRepository.findBySubmissionIdIn(List.of(submission.getId())))
                .thenReturn(List.of(gradedResult));

        ChatMessagesResponse result = chatService.getMessages(chatId);

        assertThat(result.messages()).hasSize(2);
        assertThat(result.messages().get(0)).isInstanceOf(ExerciseTimelineEntry.class);
        ExerciseTimelineEntry exerciseEntry = (ExerciseTimelineEntry) result.messages().get(0);
        assertThat(exerciseEntry.questions()).hasSize(1);
        assertThat(exerciseEntry.questions().get(0).answerKey()).isEqualTo("Hubiera ido");

        assertThat(result.messages().get(1)).isInstanceOf(SubmissionTimelineEntry.class);
        SubmissionTimelineEntry submissionEntry = (SubmissionTimelineEntry) result.messages().get(1);
        assertThat(submissionEntry.gradedResult()).isNotNull();
        assertThat(submissionEntry.gradedResult().overallScore()).isEqualTo(100);
    }
}
