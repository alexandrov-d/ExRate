package com.example.exrate.service;

import com.example.exrate.data.rest.ExRateRatesResponse;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Slf4j
@RequiredArgsConstructor
@Service
public class ExRateService {

  @Value("${app.rates.scale}")
  private int scale;

  private final FreeCurrencyProvider currencyProvider;

  /**
   * Get exchange rate relative to a given currency {@code from}
   * @param from the source currency code (3-letter ISO format, e.g., "USD")
   * @param to   the optional target currency code (3-letter ISO format, e.g., "EUR"); if {@code null}, all currency rates are returned
   */
  public ExRateRatesResponse getRates(String from, @Nullable String to) {
    Map<String, BigDecimal> rates = currencyProvider.getCurrencyRates();
    ExRateRatesResponse exRateResponse = new ExRateRatesResponse()
        .setCurrency(from);
    BigDecimal fromRate = rates.get(from);
    if (to == null) {
      for (Entry<String, BigDecimal> entry : rates.entrySet()) {
        exRateResponse.getRates()
            .put(entry.getKey(), entry.getValue().divide(fromRate, scale, RoundingMode.HALF_UP));
      }
    } else {
      exRateResponse.getRates().put(to, rates.get(to).divide(fromRate, scale, RoundingMode.HALF_UP));
    }
    return exRateResponse;
  }
}
