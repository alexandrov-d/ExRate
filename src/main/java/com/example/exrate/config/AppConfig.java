package com.example.exrate.config;

import com.example.exrate.config.AppConfig.FreeCurrencyProps;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@EnableConfigurationProperties({FreeCurrencyProps.class})
@EnableCaching
@Configuration
public class AppConfig {

  public static final String EX_RATE_CACHE_KEY = "ex-rate-api-cache";

  @ConfigurationProperties("free-currency")
  public record FreeCurrencyProps(
      String url,
      String apiKey,
      int readTimeoutMs) {
  }

  @Bean
  public RestClient freeCurrencyClient(FreeCurrencyProps props) {
    SimpleClientHttpRequestFactory factory = ClientHttpRequestFactoryBuilder.simple()
        .withCustomizer(custom -> {
          custom.setConnectTimeout(props.readTimeoutMs - 1000);
          custom.setReadTimeout(props.readTimeoutMs);
        })
        .build();
    return RestClient.builder()
        .requestFactory(factory)
        .baseUrl(props.url)
        .defaultHeader("apikey", props.apiKey)
        .build();
  }

  @Bean
  public CacheManager cacheManager(@Value("${app.rates.cacheSec}") int cacheSec) {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
    cacheManager.registerCustomCache(EX_RATE_CACHE_KEY,
        Caffeine.newBuilder().expireAfterWrite(cacheSec, TimeUnit.SECONDS).build());
    return cacheManager;
  }

  @Bean
  public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer(FreeCurrencyProps props) {
    return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
        .circuitBreakerConfig(CircuitBreakerConfig.custom()
            .slidingWindowSize(10)
            //high value as successful result expected to be cached and used for some time anyway
            .failureRateThreshold(90)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .permittedNumberOfCallsInHalfOpenState(3)
            .build())
        .timeLimiterConfig(TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofMillis(props.readTimeoutMs + 2000))
            .build())
        .build());
  }

}
