package com.example.exrate.data.freecurrency;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;

/**
 * FreeCurrency api latest rates response
 */
@Data
public class FcLatestResponse {
    private Map<String, BigDecimal> data;
}