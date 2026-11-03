package com.myshopycart.paymentservice.config;

import com.myshopycart.paymentservice.gateway.PaymentGatewayException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Configuration
public class ResilienceConfig {

    @Bean
    public TimeLimiter paymentGatewayTimeLimiter() {

        TimeLimiterConfig config =
                TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(2))
                        .cancelRunningFuture(true)
                        .build();

        return TimeLimiter.of("paymentGateway", config);
    }

    @Bean
    public Retry paymentGatewayRetry() {

        RetryConfig config =
                RetryConfig.custom()
                        .maxAttempts(3)
                        .waitDuration(Duration.ofMillis(500))
                        .retryExceptions(TimeoutException.class)
                        .build();

        return Retry.of("paymentGateway", config);
    }

    @Bean
    public CircuitBreaker paymentGatewayCircuitBreaker(
            CircuitBreakerRegistry circuitBreakerRegistry) {

        CircuitBreakerConfig config =
                CircuitBreakerConfig.custom()
                        .slidingWindowType(
                                CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                        .slidingWindowSize(5)
                        .minimumNumberOfCalls(5)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(5))
                        .permittedNumberOfCallsInHalfOpenState(2)
                        .automaticTransitionFromOpenToHalfOpenEnabled(true)

                        .recordExceptions(
                                PaymentGatewayException.class,
                                TimeoutException.class)

                        .build();

        return circuitBreakerRegistry
                .circuitBreaker("paymentGateway", config);
    }
}