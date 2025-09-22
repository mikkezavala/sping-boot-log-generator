package com.log.generator;

import com.log.generator.model.LogLevel;
import com.log.generator.model.LogScenario;
import com.log.generator.model.StructuredLogResponse;
import com.log.generator.service.SyntheticLogGeneratorService;
import com.log.generator.utils.RandomDataGenerator;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

@Component
@EnableAsync
public class ScheduledTasks {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);

  private final SyntheticLogGeneratorService logGenerator;

  @Value("${scheduled.max-parallel:8}")
  private Integer maxParallel;

  private final CircuitBreaker circuitBreaker;

  public ScheduledTasks(
      SyntheticLogGeneratorService logGenerator,
      CircuitBreaker circuitBreaker
  ) {
    this.logGenerator = logGenerator;
    this.circuitBreaker = circuitBreaker;
  }

  @Scheduled(fixedRateString = "${scheduled.task.synthetic-logs.fixed-rate:1000}")
  public void generateLogs() {
    Flux.range(0, maxParallel)
        .flatMap(_ -> {
          LogScenario scenario = selectRandomScenario();
          LogLevel level = selectWeightedLogLevel();

          return logGenerator.generateLogEntry(scenario, level).transformDeferred(
              CircuitBreakerOperator.of(circuitBreaker)
          ).retryWhen(retrySpec()).doOnNext(entry ->
              outputSyntheticLog(entry, scenario)
          ).onErrorResume(ex -> {
            LOGGER.warn("Synthetic log generation failed: {}", ex.toString());
            return Mono.empty();
          });
        }).subscribeOn(Schedulers.boundedElastic())
        .doOnError(error -> LOGGER.error("Error in scheduled log generation", error))
        .onErrorResume(_ -> Flux.empty())
        .subscribe();
  }

  private LogScenario selectRandomScenario() {
    LogScenario[] scenarios = LogScenario.values();
    return scenarios[ThreadLocalRandom.current().nextInt(scenarios.length)];
  }

  private LogLevel selectWeightedLogLevel() {
    double randomValue = ThreadLocalRandom.current().nextDouble();
    double cumulativeProbability = 0.0;
    for (LogLevel level : LogLevel.values()) {
      cumulativeProbability += level.getProbability();
      if (randomValue <= cumulativeProbability) {
        return level;
      }
    }
    return LogLevel.INFO;
  }

  private void outputSyntheticLog(StructuredLogResponse entry, LogScenario scenario) {
    try {
      String loggerName = generateLoggerName(scenario);
      Logger syntheticLogger = LoggerFactory.getLogger(loggerName);

      var syntheticMetadata = getStructuredArgument(entry);
      var metadata = entry.getMetadata() != null && !entry.getMetadata().isEmpty()
          ? StructuredArguments.keyValue("metadata", entry.getMetadata())
          : StructuredArguments.keyValue("metadata", Map.of("context", "generated"));

      switch (entry.getLevel()) {
        case TRACE -> syntheticLogger.trace(entry.getMessage(), syntheticMetadata, metadata);
        case DEBUG -> syntheticLogger.debug(entry.getMessage(), syntheticMetadata, metadata);
        case INFO -> syntheticLogger.info(entry.getMessage(), syntheticMetadata, metadata);
        case WARN -> syntheticLogger.warn(entry.getMessage(), syntheticMetadata, metadata);
        case ERROR -> {
          if (entry.getStackTrace() != null && !entry.getStackTrace().trim().isEmpty()) {
            syntheticLogger.error("{} Stack trace: {}", entry.getMessage(), entry.getStackTrace(),
                syntheticMetadata, metadata);
          } else {
            syntheticLogger.error(entry.getMessage(), syntheticMetadata, metadata);
          }
        }
      }

    } catch (Exception e) {
      LOGGER.error("Failed to output synthetic log entry", e);
    }
  }

  private RetryBackoffSpec retrySpec() {
    return Retry.backoff(2, Duration.ofMillis(100)).filter(ex ->
        ex instanceof TimeoutException || ex instanceof WebClientRequestException
    );
  }

  private static StructuredArgument getStructuredArgument(StructuredLogResponse entry) {
    String transactionId = RandomDataGenerator.generateRequestId();
    String correlationId = "corr-" + RandomDataGenerator.generateRandomId(31);

    return StructuredArguments.entries(
        Map.ofEntries(
            Map.entry("user_id", entry.getUserId()),
            Map.entry("transaction_id", transactionId),
            Map.entry("request_path", entry.getRequestPath()),
            Map.entry("service_version", entry.getServiceVersion()),
            Map.entry("duration_ms", entry.getDurationMs()),
            Map.entry("session_id", entry.getSessionId()),
            Map.entry("response_code", entry.getResponseCode()),
            Map.entry("thread_id", entry.getThreadId()),
            Map.entry("environment", entry.getEnvironment()),
            Map.entry("instance_id", entry.getInstanceId()),
            Map.entry("region", entry.getRegion()),
            Map.entry("correlation_id", correlationId)));
  }

  private String generateLoggerName(LogScenario scenario) {
    String className = switch (scenario) {
      case USER_LOGIN -> "UserAuthenticationService";
      case DATABASE_OPERATION -> "DatabaseService";
      case API_REQUEST -> "ApiController";
      case ERROR_HANDLING -> "ErrorHandlerService";
      case SECURITY_EVENT -> "SecurityService";
      case PERFORMANCE_METRIC -> "PerformanceMonitorService";
      case FILE_OPERATION -> "FileProcessorService";
      case BUSINESS_LOGIC -> "BusinessLogicService";
      case SYSTEM_STARTUP -> "ApplicationStartupService";
      case CACHE_OPERATION -> "CacheService";
    };
    return "com.synthetic." + className;
  }

}
