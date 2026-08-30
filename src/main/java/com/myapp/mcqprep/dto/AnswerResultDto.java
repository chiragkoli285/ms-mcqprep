package com.myapp.mcqprep.dto;

import java.util.Map;

public record AnswerResultDto(
        boolean correct,
        String correctOptionId,
        String selectedOptionId,
        Map<String, String> explanations // all 4 options' explanations, shown together in the UI
) {}