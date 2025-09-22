package com.log.generator.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;

@Data
@Component
@ConfigurationProperties(prefix = "ollama")
public class OllamaOptions {
  private boolean enabled;
  private String baseUrl;
  private String model;
  private int timeoutSeconds;
  private Map<String, String> formats = new HashMap<>();

  public String getFormat(String formatType) {
    return formats.get(formatType);
  }
}
