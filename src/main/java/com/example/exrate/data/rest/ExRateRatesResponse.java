package com.example.exrate.data.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;

@Schema(description = "Response object containing currency rates relative to a base currency")
@Data
public class ExRateRatesResponse extends ExRateBaseResponse {

  @Schema(description = "The base currency code", example = "USD")
  private String currency;

  @Schema(
      description = "Map of other currency codes to their exchange rates relative to the base currency",
      example = "{\"EUR\": 0.915, \"JPY\": 108.53, \"GBP\": 0.782}"
  )
  private final Map<String, BigDecimal> rates = new TreeMap<>();
}

