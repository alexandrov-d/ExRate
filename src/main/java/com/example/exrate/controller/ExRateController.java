package com.example.exrate.controller;

import com.example.exrate.data.rest.ExRateConversionsResponse;
import com.example.exrate.data.rest.ExRateRatesResponse;
import com.example.exrate.service.ExRateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/currency")
@RestController
public class ExRateController {

  private final ExRateService service;

  @GetMapping("rates")
  public ExRateRatesResponse rates(
      @RequestParam @Size(min = 3, max = 3)
      String from,
      @RequestParam(required = false)
      @Size(min = 3, max = 3)
      String to) {
    log.debug("Get exchange rate for {} currency. To: {}", from, to);
    List<String> lookupCurrencies = to != null ? List.of(from, to) : List.of(from);
    validateCurrencies(lookupCurrencies);
    return service.getRates(from, to);
  }

  @GetMapping("convert")
  public ExRateConversionsResponse convert(
      @RequestParam @Size(min = 3, max = 3) String from,
      @RequestParam BigDecimal amount,
      @RequestParam
      @Valid
      @NotEmpty
      List<@Size(min = 3, max = 3) String> to) {
    log.debug("Convert exchange rate from {} to {} request", from, to);
    List<String> lookupCurrencies = new ArrayList<>(to);
    lookupCurrencies.add(from);
    validateCurrencies(lookupCurrencies);
    return service.convert(from, to, amount);
  }

  private void validateCurrencies(List<String> requestCurrencies) {
    Set<String> availableCurrencies = service.getAvailableCurrencies();
    boolean containsAll = availableCurrencies.containsAll(requestCurrencies);
    if (!containsAll) {
      throw new IllegalArgumentException("One or more of requested currencies are not supported.");
    }
  }
}
