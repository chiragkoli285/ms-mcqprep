package com.myapp.mcqprep.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "daily_sets")
public class DailySet {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "question_ids", columnDefinition = "uuid[]")
    private List<UUID> questionIds; // Postgres array - see note below

    protected DailySet() {}

    public DailySet(UUID userId, LocalDate date, List<UUID> questionIds) {
        this.userId = userId;
        this.date = date;
        this.questionIds = questionIds;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public LocalDate getDate() { return date; }
    public List<UUID> getQuestionIds() { return questionIds; }
}