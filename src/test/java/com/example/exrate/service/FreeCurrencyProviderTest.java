package com.example.exrate.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.exrate.data.ServiceUnavailableException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

public class FreeCurrencyProviderTest {

  private MockWebServer mockWebServer;
  private CircuitBreaker circuitBreaker;

  private FreeCurrencyProvider provider;

  @BeforeEach
  void setUp() throws Exception {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    RestClient restClient = RestClient.builder()
        .baseUrl(mockWebServer.url("/").toString())
        .build();
    CircuitBreakerFactory<?, ?> factory = mock(CircuitBreakerFactory.class);
    circuitBreaker = mock(CircuitBreaker.class);
    when(factory.create("currencyApi")).thenReturn(circuitBreaker);

    provider = new FreeCurrencyProvider(restClient, factory);
  }

  @AfterEach
  void tearDown() throws Exception {
    mockWebServer.shutdown();
  }

  @Test
  void getCurrencyRates_Ok() {

    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("""
            {
              "data": {
                "EUR": 0.92,
                "JPY": 110.12
              }
            }
            """)
        .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    );

    when(circuitBreaker.run(any(), any()))
        .thenAnswer(invocation -> invocation.<Supplier<Map<String, BigDecimal>>>getArgument(0).get());

    Map<String, BigDecimal> rates = provider.getCurrencyRates();

    assertThat(rates).isNotEmpty();
    assertThat(rates).containsEntry("EUR", new BigDecimal("0.92"));
    assertThat(rates).containsEntry("JPY", new BigDecimal("110.12"));

    verify(circuitBreaker).run(any(), any());
  }

  @Test
  void getCurrencyRates_Empty_ThrowsServiceUnavailable() {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("""
            {
              "data": {}
            }
            """)
        .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    );

    when(circuitBreaker.run(any(), any())).thenAnswer(inv -> {
      Supplier<Map<String, BigDecimal>> supplier = inv.getArgument(0);
      Function<Throwable, Map<String, BigDecimal>> fallback = inv.getArgument(1);
      try {
        return supplier.get();
      } catch (Exception ex) {
        return fallback.apply(ex);
      }
    });

    assertThatThrownBy(() -> provider.getCurrencyRates())
        .isInstanceOf(ServiceUnavailableException.class)
        .hasMessageContaining("No data received from freeCurrency api");
  }
}
