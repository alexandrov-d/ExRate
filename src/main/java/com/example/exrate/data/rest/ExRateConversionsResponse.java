package com.example.exrate.data.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;

@Schema(description = "Response object containing currency conversion results")
@Data
public class ExRateConversionsResponse extends ExRateBaseResponse {

  @Schema(description = "The original source currency code", example = "USD")
  private String from;

  @Schema(description = "The original amount to be converted", example = "100.00")
  private BigDecimal amount;

  @Schema(
      description = "Map of target currency codes to their converted amounts",
      example = "{\"EUR\": 91.50, \"JPY\": 10853.00, \"GBP\": 78.20}"
  )
  private final Map<String, BigDecimal> conversions = new TreeMap<>();
}
