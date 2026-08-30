package com.myapp.mcqprep.controller;

import com.myapp.mcqprep.dto.DailySetResponseDto;
import com.myapp.mcqprep.service.QuizService;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/today")
    public DailySetResponseDto getTodaysSet(@RequestHeader("X-User-Id") UUID userId) {
        return quizService.getTodaysSet(userId);
    }

    @PostMapping("/refresh")
    public DailySetResponseDto refresh(@RequestHeader("X-User-Id") UUID userId) {
        return quizService.refresh(userId);
    }
}