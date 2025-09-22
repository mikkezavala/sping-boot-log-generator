package com.log.generator.config;


import com.log.generator.properties.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OllamaConfiguration {

  private final OllamaOptions ollamaOptions;

  public OllamaConfiguration(OllamaOptions ollamaOptions) {
    this.ollamaOptions = ollamaOptions;
  }

  @Bean("ollamaWebClient")
  public WebClient ollamaWebClient() {
    return WebClient.builder()
        .baseUrl(ollamaOptions.getBaseUrl())
        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
        .build();

  }
}
