package com.myapp.mcqprep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AnswerSubmissionRequest(
        @NotNull UUID questionId,
        @NotBlank String selectedOptionId
) {}
