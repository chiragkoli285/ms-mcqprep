package com.myapp.mcqprep.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_stats")
public class UserStats {

    @EmbeddedId
    private UserStatsId id;

    @Column(name = "correct_count")
    private int correctCount = 0;

    @Column(name = "total_count")
    private int totalCount = 0;

    @Column(name = "last_attempted")
    private Instant lastAttempted;

    protected UserStats() {}

    public UserStats(UUID userId, String topic) {
        this.id = new UserStatsId(userId, topic);
    }

    public void recordAttempt(boolean correct) {
        totalCount++;
        if (correct) correctCount++;
        lastAttempted = Instant.now();
    }

    public UserStatsId getId() { return id; }
    public int getCorrectCount() { return correctCount; }
    public int getTotalCount() { return totalCount; }
    public Instant getLastAttempted() { return lastAttempted; }

    @Embeddable
    public static class UserStatsId implements Serializable {
        @Column(name = "user_id")
        private UUID userId;
        private String topic;

        protected UserStatsId() {}
        public UserStatsId(UUID userId, String topic) { this.userId = userId; this.topic = topic; }

        public UUID getUserId() { return userId; }
        public String getTopic() { return topic; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserStatsId that)) return false;
            return userId.equals(that.userId) && topic.equals(that.topic);
        }
        @Override
        public int hashCode() { return Objects.hash(userId, topic); }
    }
}
