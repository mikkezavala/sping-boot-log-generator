package com.log.generator.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.log.generator.model.LogLevel;
import com.log.generator.model.LogScenario;
import com.log.generator.model.StructuredLogResponse;
import com.log.generator.properties.OllamaOptions;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class OllamaService {

  private final ObjectMapper objectMapper;
  private final WebClient ollamaWebClient;
  private final OllamaOptions ollamaOptions;
  private final PromptService promptService;

  private static final String REQUEST_URI = "/api/generate";
  private static final String LOG_PREFIX = "[SVC-OLLAMA]:";

  private final AtomicInteger requestCounter = new AtomicInteger(0);

  public OllamaService(
      ObjectMapper objectMapper,
      WebClient ollamaWebClient,
      OllamaOptions ollamaOptions,
      PromptService promptService
  ) {
    this.objectMapper = objectMapper;
    this.ollamaWebClient = ollamaWebClient;
    this.ollamaOptions = ollamaOptions;
    this.promptService = promptService;
  }

  public Mono<StructuredLogResponse> generateLogMessage(
      LogScenario scenario, LogLevel logLevel, Map<String, Object> contextData
  ) {
    int requestId = requestCounter.incrementAndGet();
    return callOllama(buildPrompt(scenario, logLevel, contextData)).doOnSuccess(result ->
        log.info("{} Request completed request_id={} message='{}'", LOG_PREFIX, requestId,
            result.getMessage()
        )
    );
  }

  private Mono<StructuredLogResponse> callOllama(String prompt) {
    Map<String, Object> request = new HashMap<>();
    request.put("model", ollamaOptions.getModel());
    request.put("prompt", prompt);
    request.put("stream", false);
    request.put("format", outputSchema("app-log"));
    request.put("options", Map.of(
        "temperature", 0.7,
        "num_predict", 1000,
        "top_p", 0.9,
        "repeat_penalty", 1.1
    ));

    log.info("{} Request POST={}{} Model={}", LOG_PREFIX, ollamaOptions.getBaseUrl(), REQUEST_URI,
        ollamaOptions.getModel());

    return ollamaWebClient.post()
        .uri(REQUEST_URI)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(String.class)
        .map(this::extractResponse);
  }

  private StructuredLogResponse extractResponse(String jsonResponse) {
    try {
      JsonNode node = objectMapper.readTree(jsonResponse).get("response");
      JsonNode responseNode = parseJsonNode(node);

      return objectMapper.treeToValue(responseNode, StructuredLogResponse.class);
    } catch (Exception ex) {
      log.error("{} Failed to parse JSON: {}", LOG_PREFIX, ex.getMessage());
      throw new RuntimeException("LLM response parsing failed", ex);
    }
  }

  private JsonNode parseJsonNode(JsonNode jsonNode) {
    try {
      String response = jsonNode.asText();
      JsonNode responseNode = objectMapper.readTree(response);
      if (responseNode.has("metadata")) {
        JsonNode metadataNode = responseNode.get("metadata");
        if (metadataNode.isNull() || metadataNode.asText().trim().isEmpty()) {
          ((ObjectNode) responseNode).remove("metadata");
        }
      }
      return responseNode;
    } catch (Exception ex) {
      log.error("{} Failed to parse response node: {}", LOG_PREFIX, ex.getMessage());
      throw new RuntimeException("LLM response parsing failed", ex);
    }
  }

  private String buildPrompt(LogScenario scenario, LogLevel logLevel,
      Map<String, Object> contextData) {
    return promptService.buildPrompt(scenario, logLevel, contextData);
  }

  private Map<String, Object> outputSchema(String formatType) {
    try {
      return objectMapper.readValue(ollamaOptions.getFormat(formatType), new TypeReference<>() {
      });
    } catch (Exception e) {
      log.error("{} Failed to parse {} schema: {}", LOG_PREFIX, formatType, e.getMessage());
      return null;
    }
  }
}
