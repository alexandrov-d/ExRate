package com.example.exrate.service;

import com.example.exrate.config.AppConfig;
import com.example.exrate.data.ServiceUnavailableException;
import com.example.exrate.data.freecurrency.FcLatestResponse;
import java.math.BigDecimal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


/**
 * Client for FreeCurrency.com exchange rate service
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class FreeCurrencyProvider {

  private final RestClient freeCurrencyClient;
  private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

  /**
   * @return USD based currency rate map(Currency => Rate)
   */
  @Cacheable(AppConfig.EX_RATE_CACHE_KEY)
  public Map<String, BigDecimal> getCurrencyRates() {
    CircuitBreaker currencyApi = circuitBreakerFactory.create("currencyApi");
    log.info("Get currency rates from FreeCurrency.com endpoint");
    return currencyApi.run(() -> {
      FcLatestResponse body = freeCurrencyClient.get()
          .uri("/latest")
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(FcLatestResponse.class);
      if (body == null || body.getData().isEmpty()) {
        throw new ServiceUnavailableException("No data received from freeCurrency api");
      }
      return body.getData();
    }, ex -> {
      log.error("Error fetching exchange rates: {}", ex.getMessage());
      // Can fall back to another exchange rate source as improvement
      if (ex instanceof ServiceUnavailableException unavailableException) {
        throw unavailableException;
      }
      throw new ServiceUnavailableException("Failed to fetch currency from freeCurrency provider", ex);
    });
  }
}
