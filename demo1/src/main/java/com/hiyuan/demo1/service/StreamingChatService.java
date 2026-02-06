package com.hiyuan.demo1.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingChatService {

    private static final ParameterizedTypeReference<ServerSentEvent<String>> SSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WebClient.Builder webClientBuilder;
    private final AiProviderModelManager modelManager;
    private final ObjectMapper objectMapper;

    public Flux<String> streamChatCompletion(String prompt) {
        AiProviderModelManager.ChatModelSettings settings = modelManager.getCurrentModelSettings();
        WebClient client = webClientBuilder.baseUrl(settings.baseUrl()).build();

        Map<String, Object> body = Map.of(
                "model", settings.modelName(),
                "temperature", settings.temperature(),
                "max_tokens", settings.maxTokens(),
                "stream", true,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful teaching assistant."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        return client.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + settings.apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(SSE_TYPE)
                .map(ServerSentEvent::data)
                .filter(Objects::nonNull)
                .flatMap(this::extractChunks)
                .onErrorResume(ex -> {
                    log.error("调用流式模型失败: {}", ex.getMessage(), ex);
                    return Flux.error(new RuntimeException("流式模型调用失败: " + ex.getMessage(), ex));
                });
    }

    private Flux<String> extractChunks(String data) {
        String trimmed = data.trim();
        if ("[DONE]".equals(trimmed)) {
            return Flux.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(data);
            JsonNode choices = root.path("choices");
            if (!choices.isArray()) {
                return Flux.empty();
            }

            List<String> chunks = new ArrayList<>();
            for (JsonNode choice : choices) {
                String content = choice.path("delta").path("content").asText("");
                if (StringUtils.hasText(content)) {
                    chunks.add(content);
                }
            }

            return Flux.fromIterable(chunks);
        } catch (Exception e) {
            log.warn("解析流式响应失败: {}", e.getMessage());
            return Flux.empty();
        }
    }
}
