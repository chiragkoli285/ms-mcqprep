package com.myapp.mcqprep.controller;

import com.myapp.mcqprep.dto.AnswerResultDto;
import com.myapp.mcqprep.dto.AnswerSubmissionRequest;
import com.myapp.mcqprep.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/answers")
public class AnswerController {

    private final QuizService quizService;

    public AnswerController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    public AnswerResultDto submitAnswer(@RequestHeader("X-User-Id") UUID userId,
                                        @Valid @RequestBody AnswerSubmissionRequest request) {
        return quizService.submitAnswer(userId, request);
    }
}