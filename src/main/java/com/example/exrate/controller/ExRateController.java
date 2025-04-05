package com.example.exrate.controller;

import com.example.exrate.data.rest.ExRateRatesResponse;
import com.example.exrate.service.ExRateService;
import jakarta.validation.constraints.Size;
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
    return service.getRates(from, to);
  }
}
