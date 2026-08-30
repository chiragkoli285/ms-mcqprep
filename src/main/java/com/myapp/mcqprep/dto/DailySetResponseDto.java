package com.myapp.mcqprep.dto;

import java.time.LocalDate;
import java.util.List;

public record DailySetResponseDto(
        LocalDate date,
        List<QuestionResponseDto> questions
) {}