package com.myapp.mcqprep.service;

import com.myapp.mcqprep.dto.LlmGeneratedQuestion;
import com.myapp.mcqprep.dto.OptionDto;
import com.myapp.mcqprep.entity.Question;
import com.myapp.mcqprep.llm.GeminiClient;
import com.myapp.mcqprep.repository.QuestionRepository;
import com.myapp.mcqprep.repository.UserSeenQuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuestionGenerationService {

    private static final Logger log = LoggerFactory.getLogger(QuestionGenerationService.class);

    private final GeminiClient geminiClient;
    private final QuestionRepository questionRepository;
    private final UserSeenQuestionRepository seenRepository;
    private final DedupService dedupService;

    public QuestionGenerationService(GeminiClient geminiClient, QuestionRepository questionRepository,
                                     UserSeenQuestionRepository seenRepository, DedupService dedupService) {
        this.geminiClient = geminiClient;
        this.questionRepository = questionRepository;
        this.seenRepository = seenRepository;
        this.dedupService = dedupService;
    }

    public List<Question> generateUniqueSet(UUID userId, int count) {
        List<String> recentHashes = seenRepository.findRecentHashesForUser(userId);
        List<Question> result = new ArrayList<>();

        int attempts = 0;
        int maxAttempts = 3;

        while (result.size() < count && attempts < maxAttempts) {
            int needed = count - result.size();
            String prompt = buildPrompt(needed, recentHashes);
            List<LlmGeneratedQuestion> generated = geminiClient.generateQuestions(prompt);

            for (LlmGeneratedQuestion gq : generated) {
                // ============ NEW GUARD CLAUSE START ============
                if (!isStructurallyValid(gq)) {
                    log.warn("Skipping malformed question from LLM: {}", gq.question());
                    continue;
                }
                // ============ NEW GUARD CLAUSE END ============

                String hash = dedupService.hash(gq.question(), gq.code());
                if (questionRepository.existsByContentHash(hash)) {
                    continue;
                }
                Question entity = new Question(
                        gq.topic(), "mid-senior", gq.question(), gq.code(),
                        gq.options(), gq.correctOptionId(), gq.explanations(), hash
                );
                result.add(questionRepository.save(entity));
                recentHashes.add(hash);
            }
            attempts++;
        }
        return result;
    }

    // ============ NEW METHOD START ============
    private boolean isStructurallyValid(LlmGeneratedQuestion gq) {
        if (gq.options() == null || gq.options().size() != 4) return false;

        Set<String> optionIds = gq.options().stream()
                .map(OptionDto::id)
                .collect(Collectors.toSet());
        if (optionIds.size() != 4) return false; // duplicate ids

        if (gq.correctOptionId() == null || !optionIds.contains(gq.correctOptionId())) return false;

        if (gq.explanations() == null || !gq.explanations().keySet().containsAll(optionIds)) return false;

        return true;
    }
    // ============ NEW METHOD END ============

    private String buildPrompt(int count, List<String> excludeHashes) {
        return """
            You are generating interview-prep MCQs for a Java Backend Developer with 4-5 years
            of professional experience. Generate %d questions covering Core Java, Spring Boot,
            JVM internals, databases, and system design fundamentals. At least 40%% must include
            a realistic Java code snippet. Avoid repeating topics already covered recently (hashes: %s).
            Return ONLY valid JSON matching the agreed schema, no markdown fences.
            """.formatted(count, excludeHashes.size());
    }
}