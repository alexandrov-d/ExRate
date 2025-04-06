package com.example.exrate.controller;

import com.example.exrate.data.rest.ExRateConversionsResponse;
import com.example.exrate.data.rest.ExRateRatesResponse;
import com.example.exrate.service.ExRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Currency Exchange API", description = "Provides endpoints to retrieve exchange rates and perform currency conversions")
@RestController
public class ExRateController {

  private final ExRateService service;

  @Operation(
      summary = "Get currency rates for a requested base currency",
      description = "Returns the exchange rates of the given `from` currency relative to multiple target currencies. " +
          "The `from` parameter must be a 3-letter ISO 4217 currency code (e.g., 'USD', 'EUR').",
      responses = {
          @ApiResponse(responseCode = "200", description = "Successfully retrieved exchange rates"),
          @ApiResponse(responseCode = "400", description = "Invalid currency code provided"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "503", description = "Service unavailable at the current moment")
      }
  )
  @GetMapping("rates")
  public ExRateRatesResponse rates(
      @Parameter(
          description = "The base currency code to get rates for (3-letter ISO 4217 code)",
          required = true,
          example = "USD"
      )
      @RequestParam @Size(min = 3, max = 3)
      String from,

      @Parameter(description = "The base currency code to get rates for (3-letter ISO 4217 code). If null all available will be returned")
      @RequestParam(required = false)
      @Size(min = 3, max = 3)
      String to) {
    log.debug("Get exchange rate for {} currency. To: {}", from, to);
    List<String> lookupCurrencies = to != null ? List.of(from, to) : List.of(from);
    validateCurrencies(lookupCurrencies);
    return service.getRates(from, to);
  }

  @Operation(
      summary = "Convert currency amount to multiple target currencies",
      description = "Converts a given amount from a source currency (`from`) to a list of target currencies (`to`). " +
          "Returns the equivalent amounts based on the current exchange rates.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Successfully converted amount to target currencies"),
          @ApiResponse(responseCode = "400", description = "Invalid input (e.g., missing or incorrect currency codes)"),
          @ApiResponse(responseCode = "503", description = "Service unavailable at this moment")
      }
  )
  @GetMapping("convert")
  public ExRateConversionsResponse convert(
      @Parameter(
          description = "The source currency code (e.g., 'USD', 'EUR'). ISO 4217",
          required = true,
          example = "USD"
      )
      @RequestParam @Size(min = 3, max = 3) String from,
      @Parameter(
          description = "The amount to convert from the source currency",
          required = true,
          example = "100.00"
      )
      @RequestParam BigDecimal amount,
      @Parameter(
          description = "List of target currency codes to convert the amount into. ISO 4217",
          required = true,
          example = "[\"EUR\", \"JPY\", \"GBP\"]"
      )
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
