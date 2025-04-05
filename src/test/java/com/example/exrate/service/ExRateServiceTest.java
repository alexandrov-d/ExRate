package com.example.exrate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.exrate.data.rest.ExRateRatesResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class ExRateServiceTest {

  private Map<String, BigDecimal> mockRates;
  private final int scale = 4;
  private final String USD = "USD";
  private final String EUR = "EUR";

  @Mock
  private FreeCurrencyProvider provider;

  @InjectMocks
  private ExRateService exRateService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(exRateService, "scale", scale);

    mockRates = new HashMap<>();
    mockRates.put("USD", BigDecimal.valueOf(1.0));
    mockRates.put("EUR", BigDecimal.valueOf(0.911));
    mockRates.put("GBP", BigDecimal.valueOf(0.77));
    mockRates.put("JPY", BigDecimal.valueOf(146.0));

    when(provider.getCurrencyRates()).thenReturn(mockRates);
  }

  @Test
  void getRates_ToIsNullable_ReturnAllRates() {
    ExRateRatesResponse result = exRateService.getRates(USD, null);

    assertThat(result.getCurrency()).isEqualTo(USD);

    Map<String, BigDecimal> rates = result.getRates();
    assertThat(rates).hasSize(4);

    assertThat(rates.get(USD)).isEqualByComparingTo(BigDecimal.ONE);
    assertThat(rates.get(EUR)).isEqualByComparingTo(new BigDecimal("0.9110"));
    assertThat(rates.get("GBP")).isEqualByComparingTo(new BigDecimal("0.7700"));
    assertThat(rates.get("JPY")).isEqualByComparingTo(new BigDecimal("146.0000"));
  }

  @Test
  void getRates_fromNonUsdCurrency_RatesOk() {
    ExRateRatesResponse result = exRateService.getRates(EUR, null);

    assertThat(result).isNotNull();
    assertThat(result.getCurrency()).isEqualTo(EUR);

    Map<String, BigDecimal> rates = result.getRates();

    assertThat(rates.get(USD)).isEqualByComparingTo(
        mockRates.get(USD).divide(mockRates.get(EUR), scale, RoundingMode.HALF_UP));
    assertThat(rates.get(EUR)).isEqualByComparingTo(BigDecimal.ONE);
    assertThat(rates.get("GBP")).isEqualByComparingTo(
        mockRates.get("GBP").divide(mockRates.get(EUR), scale, RoundingMode.HALF_UP));
  }

  @Test
  void getRates_withToCurrency_ReturnSpecificRate() {
    ExRateRatesResponse result = exRateService.getRates(USD, EUR);

    assertThat(result).isNotNull();
    assertThat(result.getCurrency()).isEqualTo(USD);

    Map<String, BigDecimal> rates = result.getRates();
    assertThat(rates).hasSize(1);
    assertThat(rates.get(EUR)).isEqualByComparingTo(
        mockRates.get(EUR).divide(mockRates.get(USD), scale, RoundingMode.HALF_UP));
  }
}