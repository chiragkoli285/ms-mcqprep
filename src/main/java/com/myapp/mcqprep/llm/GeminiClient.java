package com.myapp.mcqprep.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.mcqprep.dto.LlmGeneratedQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GeminiClient(WebClient.Builder builder, ObjectMapper objectMapper,
                        @Value("${gemini.api-key}") String apiKey,
                        @Value("${gemini.model}") String model,
                        @Value("${gemini.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public List<LlmGeneratedQuestion> generateQuestions(String prompt) {
        Map<String, Object> requestBody = buildRequestBody(prompt);

        GeminiResponse response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/{model}:generateContent")
                        .queryParam("key", apiKey)
                        .build(model))
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.value() == 429, res ->
                        res.bodyToMono(String.class).map(body -> {
                            log.warn("Gemini rate limit hit: {}", body);
                            return new RateLimitException("Gemini rate limit exceeded");
                        }))
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), res ->
                        res.bodyToMono(String.class).map(body -> {
                            log.error("Gemini API error {}: {}", res.statusCode(), body);
                            return new IllegalStateException("Gemini API call failed: " + body);
                        }))
                .bodyToMono(GeminiResponse.class)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))
                        .filter(this::isRetryable))
                .timeout(Duration.ofSeconds(30))
                .block();

        return parseQuestions(response);
    }

    // ============ CHANGED METHOD START ============
    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> optionSchema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "id", Map.of("type", "STRING"),
                        "text", Map.of("type", "STRING")
                ),
                "required", List.of("id", "text")
        );

        Map<String, Object> explanationsSchema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "a", Map.of("type", "STRING"),
                        "b", Map.of("type", "STRING"),
                        "c", Map.of("type", "STRING"),
                        "d", Map.of("type", "STRING")
                ),
                "required", List.of("a", "b", "c", "d")
        );

        Map<String, Object> questionSchema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "topic", Map.of("type", "STRING"),
                        "question", Map.of("type", "STRING"),
                        "code", Map.of("type", "STRING", "nullable", true),
                        "options", Map.of(
                                "type", "ARRAY",
                                "items", optionSchema,
                                "minItems", 4,
                                "maxItems", 4
                        ),
                        "correct_option_id", Map.of("type", "STRING"),
                        "explanations", explanationsSchema
                ),
                "required", List.of("topic", "question", "options", "correct_option_id", "explanations")
        );

        Map<String, Object> responseSchema = Map.of(
                "type", "ARRAY",
                "items", questionSchema
        );

        return Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "temperature", 0.9,
                        "responseMimeType", "application/json",
                        "responseSchema", responseSchema
                )
        );
    }
    // ============ CHANGED METHOD END ============

    private List<LlmGeneratedQuestion> parseQuestions(GeminiResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new IllegalStateException("Gemini returned no candidates");
        }

        String rawText = response.candidates().get(0).content().parts().get(0).text();
        String cleaned = stripMarkdownFences(rawText);
        log.warn("Gemini raw response (temporary debug logging): {}", cleaned); // TEMP: remove once schema is confirmed stable

        try {
            return objectMapper.readValue(cleaned, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, LlmGeneratedQuestion.class));
        } catch (Exception e) {
            log.error("Failed to parse Gemini response as JSON. Raw text: {}", rawText, e);
            throw new IllegalStateException("Malformed JSON from Gemini, cannot parse questions", e);
        }
    }

    private String stripMarkdownFences(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(json)?", "").trim();
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
            }
        }
        return trimmed;
    }

    private boolean isRetryable(Throwable throwable) {
        return throwable instanceof RateLimitException
                || throwable instanceof WebClientResponseException.ServiceUnavailable
                || throwable instanceof WebClientResponseException.InternalServerError;
    }

    public static class RateLimitException extends RuntimeException {
        public RateLimitException(String message) { super(message); }
    }
}