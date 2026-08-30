package com.myapp.mcqprep.entity;

import com.myapp.mcqprep.dto.OptionDto;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String difficulty;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet; // nullable - only set for code-based MCQs

    @Type(JsonType.class)
    @Column(name = "options", columnDefinition = "jsonb", nullable = false)
    private List<OptionDto> options;

    @Column(name = "correct_option_id", nullable = false)
    private String correctOptionId;

    @Type(JsonType.class)
    @Column(name = "explanations", columnDefinition = "jsonb", nullable = false)
    private Map<String, String> explanations; // key = option id ("a","b","c","d")

    @Column(name = "content_hash", nullable = false, unique = true)
    private String contentHash;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    protected Question() {} // JPA requires a no-arg constructor

    public Question(String topic, String difficulty, String questionText, String codeSnippet,
                    List<OptionDto> options, String correctOptionId,
                    Map<String, String> explanations, String contentHash) {
        this.topic = topic;
        this.difficulty = difficulty;
        this.questionText = questionText;
        this.codeSnippet = codeSnippet;
        this.options = options;
        this.correctOptionId = correctOptionId;
        this.explanations = explanations;
        this.contentHash = contentHash;
    }

    // getters only - entities are read-mostly here; add setters if you need mutation later
    public UUID getId() { return id; }
    public String getTopic() { return topic; }
    public String getDifficulty() { return difficulty; }
    public String getQuestionText() { return questionText; }
    public String getCodeSnippet() { return codeSnippet; }
    public List<OptionDto> getOptions() { return options; }
    public String getCorrectOptionId() { return correctOptionId; }
    public Map<String, String> getExplanations() { return explanations; }
    public String getContentHash() { return contentHash; }
    public Instant getCreatedAt() { return createdAt; }
}