package com.myapp.mcqprep.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_seen_questions")
public class UserSeenQuestion {

    @EmbeddedId
    private UserSeenQuestionId id;

    @Column(name = "seen_at")
    private Instant seenAt = Instant.now();

    @Column(name = "answered_correctly")
    private Boolean answeredCorrectly;

    protected UserSeenQuestion() {}

    public UserSeenQuestion(UUID userId, String questionHash) {
        this.id = new UserSeenQuestionId(userId, questionHash);
    }

    public UserSeenQuestionId getId() { return id; }
    public Instant getSeenAt() { return seenAt; }
    public Boolean getAnsweredCorrectly() { return answeredCorrectly; }
    public void setAnsweredCorrectly(Boolean answeredCorrectly) { this.answeredCorrectly = answeredCorrectly; }

    @Embeddable
    public static class UserSeenQuestionId implements Serializable {
        @Column(name = "user_id")
        private UUID userId;

        @Column(name = "question_hash")
        private String questionHash;

        protected UserSeenQuestionId() {}

        public UserSeenQuestionId(UUID userId, String questionHash) {
            this.userId = userId;
            this.questionHash = questionHash;
        }

        public UUID getUserId() { return userId; }
        public String getQuestionHash() { return questionHash; }

        // equals/hashCode are required on composite key classes - JPA uses them for identity
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserSeenQuestionId that)) return false;
            return userId.equals(that.userId) && questionHash.equals(that.questionHash);
        }

        @Override
        public int hashCode() { return Objects.hash(userId, questionHash); }
    }
}