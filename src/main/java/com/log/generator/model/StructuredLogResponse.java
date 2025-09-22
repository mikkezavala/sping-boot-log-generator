package com.log.generator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StructuredLogResponse {

  @JsonProperty("message")
  private String message;

  @JsonProperty("level")
  private LogLevel level;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("transaction_id")
  private String transactionId;

  @JsonProperty("request_path")
  private String requestPath;

  @JsonProperty("service_version")
  private String serviceVersion;

  @JsonProperty("duration_ms")
  private Integer durationMs;

  @JsonProperty("session_id")
  private String sessionId;

  @JsonProperty("response_code")
  private Integer responseCode;

  @JsonProperty("thread_id")
  private String threadId;

  @JsonProperty("environment")
  private String environment;

  @JsonProperty("instance_id")
  private String instanceId;

  @JsonProperty("region")
  private String region;

  @JsonProperty("correlation_id")
  private String correlationId;

  @JsonProperty(value = "metadata", required = false)
  private Map<String, Object> metadata;

  @JsonProperty(value = "stack_trace", required = false)
  private String stackTrace;

}
