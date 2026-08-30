package com.myapp.mcqprep.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record LlmGeneratedQuestion(
        String topic,
        String question,
        String code,
        List<OptionDto> options,
        @JsonProperty("correct_option_id") String correctOptionId,
        Map<String, String> explanations
) {}
