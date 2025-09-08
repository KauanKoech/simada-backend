package com.simada_backend.integrations;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GroqClient {

    private final WebClient groqWebClient;

    @Value("${groq.model}")
    private String model;

    @Value("${groq.maxTokens:400}")
    private int maxTokens;

    @Value("${groq.temperature:0.2}")
    private double temperature;

    @Value("${GROQ_API_KEY:}")
    private String apiKey;

    public Mono<String> chat(String systemPrompt, String userPrompt) {
        ChatRequest req = ChatRequest.builder()
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .messages(List.of(
                        new ChatMessage("system", systemPrompt),
                        new ChatMessage("user", userPrompt)
                ))
                .build();

        return groqWebClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class).flatMap(body ->
                                Mono.error(new RuntimeException("Erro Groq " + resp.statusCode() + ": " + body))
                        )
                )
                .bodyToMono(ChatResponse.class)
                .doOnNext(resp -> System.out.println("[Groq OK] " + (resp != null ? "choices=" + (resp.choices != null ? resp.choices.size() : 0) : "null")))
                .map(resp -> (resp != null && resp.choices != null && !resp.choices.isEmpty())
                        ? resp.choices.get(0).message.content
                        : null
                );

    }

    // ===== payloads =====
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class ChatRequest {
        String model;
        Double temperature;
        @JsonProperty("max_tokens")
        Integer maxTokens;
        List<ChatMessage> messages;
    }

    @Data
    static class ChatMessage {
        String role;
        String content;

        public ChatMessage() {
        }

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    @Data
    static class ChatResponse {
        List<Choice> choices;

        @Data
        static class Choice {
            ChatMessage message;
        }
    }
}