package com.example.exrate.data.rest;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;

@Data
public class ExRateRatesResponse extends ExRateBaseResponse {

  private String currency;
  private final Map<String, BigDecimal> rates = new TreeMap<>();
}

