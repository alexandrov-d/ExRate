package com.example.exrate.data.rest;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;

@Data
public class ExRateConversionsResponse extends ExRateBaseResponse {

  private String from;
  private BigDecimal amount;
  private final Map<String, BigDecimal> conversions = new TreeMap<>();
}
