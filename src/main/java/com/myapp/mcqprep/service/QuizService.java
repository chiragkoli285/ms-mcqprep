package com.myapp.mcqprep.service;

import com.myapp.mcqprep.dto.*;
import com.myapp.mcqprep.entity.*;
import com.myapp.mcqprep.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuestionGenerationService generationService;
    private final QuestionRepository questionRepository;
    private final DailySetRepository dailySetRepository;
    private final UserSeenQuestionRepository seenRepository;
    private final UserStatsRepository statsRepository;
    private final DedupService dedupService;

    public QuizService(QuestionGenerationService generationService, QuestionRepository questionRepository,
                       DailySetRepository dailySetRepository, UserSeenQuestionRepository seenRepository,
                       UserStatsRepository statsRepository, DedupService dedupService) {
        this.generationService = generationService;
        this.questionRepository = questionRepository;
        this.dailySetRepository = dailySetRepository;
        this.seenRepository = seenRepository;
        this.statsRepository = statsRepository;
        this.dedupService = dedupService;
    }

    @Transactional
    public DailySetResponseDto getTodaysSet(UUID userId) {
        LocalDate today = LocalDate.now();
        DailySet set = dailySetRepository.findTopByUserIdAndDateOrderByIdDesc(userId, today)
                .orElseGet(() -> generateAndPersistSet(userId, today));
        return toResponseDto(set);
    }

    @Transactional
    public DailySetResponseDto refresh(UUID userId) {
        // always generates a fresh set, doesn't reuse today's existing one
        DailySet set = generateAndPersistSet(userId, LocalDate.now());
        return toResponseDto(set);
    }

    private DailySet generateAndPersistSet(UUID userId, LocalDate date) {
        List<Question> questions = generationService.generateUniqueSet(userId, 18);
        questions.forEach(q -> seenRepository.save(new UserSeenQuestion(userId, q.getContentHash())));
        List<UUID> ids = questions.stream().map(Question::getId).collect(Collectors.toList());
        return dailySetRepository.save(new DailySet(userId, date, ids));
    }

    private DailySetResponseDto toResponseDto(DailySet set) {
        List<QuestionResponseDto> questions = set.getQuestionIds().stream()
                .map(id -> questionRepository.findById(id).orElseThrow())
                .map(q -> new QuestionResponseDto(q.getId(), q.getTopic(), q.getQuestionText(),
                        q.getCodeSnippet(), q.getOptions()))
                .collect(Collectors.toList());
        return new DailySetResponseDto(set.getDate(), questions);
    }

    @Transactional
    public AnswerResultDto submitAnswer(UUID userId, AnswerSubmissionRequest request) {
        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new NoSuchElementException("Question not found"));

        boolean correct = question.getCorrectOptionId().equals(request.selectedOptionId());

        // update seen-question record with the outcome
        seenRepository.findById(new UserSeenQuestion.UserSeenQuestionId(userId, question.getContentHash()))
                .ifPresent(seen -> seen.setAnsweredCorrectly(correct));

        // update per-topic stats
        UserStats.UserStatsId statsId = new UserStats.UserStatsId(userId, question.getTopic());
        UserStats stats = statsRepository.findById(statsId)
                .orElseGet(() -> new UserStats(userId, question.getTopic()));
        stats.recordAttempt(correct);
        statsRepository.save(stats);

        return new AnswerResultDto(correct, question.getCorrectOptionId(),
                request.selectedOptionId(), question.getExplanations());
    }
}
