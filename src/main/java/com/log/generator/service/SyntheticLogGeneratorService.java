package com.log.generator.service;

import com.log.generator.model.LogLevel;
import com.log.generator.model.LogScenario;
import com.log.generator.model.StructuredLogResponse;
import com.log.generator.utils.RandomDataGenerator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
public class SyntheticLogGeneratorService {

  private final OllamaService ollamaService;
  private final AtomicLong entryCounter = new AtomicLong(0);

  private static final int MAX_PARALLEL = 10;
  private static final int MAX_QUEUE = 100;
  private static final String SCHEDULER_NAME = "SYN-LOG-GEN";
  private static final String LOG_PREFIX = "[SVC-GENERATOR]:";


  @Autowired
  public SyntheticLogGeneratorService(OllamaService ollamaService) {
    this.ollamaService = ollamaService;
  }

  public Mono<StructuredLogResponse> generateLogEntry(LogScenario scenario, LogLevel level) {
    long entryId = entryCounter.incrementAndGet();

    log.info(
        "{} Generate log entry: entry_id={}, scenario={}, level={}", LOG_PREFIX, entryId,
        scenario.name(), level.name()
    );

    return Mono.fromCallable(() -> generateEnhancedContextData(scenario))
        .subscribeOn(Schedulers.newBoundedElastic(MAX_PARALLEL, MAX_QUEUE, SCHEDULER_NAME)
        ).flatMap(contextData ->
            ollamaService.generateLogMessage(scenario, level, contextData)
        ).doOnSuccess(response -> log.info(
                "{} Log entry generated entry_id={} level={} message_length={}",
                LOG_PREFIX,
                entryId,
                response.getLevel(),
                response.getMessage().length()
            )
        );
  }

  private Map<String, Object> generateEnhancedContextData(LogScenario scenario) {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    Map<String, Object> context = new HashMap<>();

    context.put("request_id", RandomDataGenerator.generateRequestId());
    context.put("thread_id", RandomDataGenerator.generateThreadId());

    switch (scenario) {
      case USER_LOGIN:
        context.put("ip_address", RandomDataGenerator.generateRandomIP());
        context.put("user_agent", "Mozilla/5.0 (compatible)");
        context.put("login_attempts", random.nextInt(1, 4));
        break;

      case DATABASE_OPERATION:
        context.put("query_time", random.nextInt(10, 5000) + "ms");
        context.put("rows_affected", random.nextInt(0, 1000));
        context.put("connection_pool_size", random.nextInt(5, 50));
        break;

      case API_REQUEST:
        context.put("response_time", random.nextInt(50, 2000) + "ms");
        context.put("status_code", RandomDataGenerator.getRandomStatusCode());
        context.put("content_length", random.nextInt(100, 10000));
        break;

      case ERROR_HANDLING:
        context.put("error_code", "ERR_" + random.nextInt(1000, 9999));
        context.put("retry_count", random.nextInt(0, 5));
        context.put("severity", RandomDataGenerator.getRandomSeverity());
        break;

      case SECURITY_EVENT:
        context.put("risk_score", random.nextInt(1, 101));
        context.put("source_ip", RandomDataGenerator.generateRandomIP());
        context.put("event_type", "SECURITY_ALERT");
        break;

      case PERFORMANCE_METRIC:
        context.put("cpu_usage", random.nextInt(10, 100) + "%");
        context.put("memory_usage", random.nextInt(100, 8000) + "MB");
        context.put("gc_time", random.nextInt(1, 500) + "ms");
        break;

      case SYSTEM_STARTUP:
        context.put("startup_time", random.nextInt(1000, 30000) + "ms");
        context.put("active_profiles", "production,monitoring");
        context.put("port", RandomDataGenerator.generatePort());
        break;

      case CACHE_OPERATION:
        context.put("cache_name", "user-cache-" + random.nextInt(1, 5));
        context.put("hit_ratio", random.nextInt(60, 95) + "%");
        context.put("cache_size", random.nextInt(100, 10000));
        break;

      case FILE_OPERATION:
        context.put("file_size", random.nextInt(1024, 1048576) + "bytes");
        context.put("file_type", RandomDataGenerator.getRandomFileType());
        context.put("processing_time", random.nextInt(100, 5000) + "ms");
        break;

      case BUSINESS_LOGIC:
        context.put("order_id", "ORD-" + random.nextInt(100000, 999999));
        context.put("amount", "$" + random.nextInt(10, 10000));
        context.put("processing_time", random.nextInt(500, 3000) + "ms");
        break;
    }

    context.put("entry_id", entryCounter.get());
    context.put("service_version", RandomDataGenerator.generateServiceVersion());
    context.put("environment", RandomDataGenerator.generateEnvironment());
    context.put("datacenter", RandomDataGenerator.generateDatacenter());
    context.put("instance_id", RandomDataGenerator.generateInstanceId());

    return context;
  }
}
