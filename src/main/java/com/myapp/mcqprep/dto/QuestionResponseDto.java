package com.myapp.mcqprep.dto;

import java.util.List;
import java.util.UUID;

public record QuestionResponseDto(
        UUID id,
        String topic,
        String questionText,
        String codeSnippet,
        List<OptionDto> options
        // deliberately no correctOptionId, no explanations
) {}
