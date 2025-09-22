package com.log.generator.service;

import com.log.generator.model.LogScenario;
import com.log.generator.model.LogLevel;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Slf4j
@Service
public class PromptService {

  private String basePrompt;
  private static final String LOG_PREFIX = "[SVC-PROMPT]:";

  @PostConstruct
  public void loadPrompts() {
    try {
      ClassPathResource resource = new ClassPathResource("prompts/base-prompt.txt");
      try (InputStream inputStream = resource.getInputStream()) {
        basePrompt = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        log.info("{} Base prompt loaded successfully", LOG_PREFIX);
      }
    } catch (IOException e) {
      log.error("{} Failed to load base prompt", LOG_PREFIX, e);
      throw new RuntimeException("Failed to load base prompt", e);
    }
  }

  public String buildPrompt(LogScenario scenario, LogLevel logLevel, Map<String, Object> contextData) {
    String prompt = basePrompt.replace("{SCENARIO}", scenario.name());

    if (logLevel != null) {
      prompt = prompt.replace("\"level\": \"INFO|WARN|ERROR|DEBUG|TRACE\"", 
                             "\"level\": \"" + logLevel.name() + "\"");
    }

    if (contextData != null && !contextData.isEmpty()) {
      StringBuilder contextBuilder = new StringBuilder();
      contextBuilder.append("\n\nContext: ");
      contextData.entrySet().stream().limit(3).forEach(entry ->
          contextBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append(" ")
      );
      prompt = prompt + contextBuilder;
    }

    log.debug("{} Generated prompt: scenario={} level={}", LOG_PREFIX, scenario.name(), 
              logLevel != null ? logLevel.name() : "AUTO");
    return prompt;
  }
}
