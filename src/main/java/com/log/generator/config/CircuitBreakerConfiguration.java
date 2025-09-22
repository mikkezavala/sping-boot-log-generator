package com.log.generator.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CircuitBreakerConfiguration {

  @Bean
  public CircuitBreaker ollamaCircuitBreaker(CircuitBreakerRegistry registry) {
    CircuitBreaker cb = registry.circuitBreaker("ollama");

    cb.getEventPublisher()
        .onStateTransition(event ->
            log.warn("Circuit breaker state transition from={} to={} creation_time={}",
                event.getStateTransition().getFromState(),
                event.getStateTransition().getToState(),
                event.getCreationTime()))
        .onFailureRateExceeded(event ->
            log.error("Circuit breaker failure rate exceeded rate={} threshold={}",
                event.getFailureRate(), cb.getCircuitBreakerConfig().getFailureRateThreshold()))
        .onSlowCallRateExceeded(event ->
            log.warn("Circuit breaker slow call rate exceeded rate={} threshold={}",
                event.getSlowCallRate(), cb.getCircuitBreakerConfig().getSlowCallRateThreshold()))
        .onCallNotPermitted(event ->
            log.info("Circuit breaker call not permitted - Ollama protection active creation_time={}",
                event.getCreationTime()));
    return cb;
  }
}
