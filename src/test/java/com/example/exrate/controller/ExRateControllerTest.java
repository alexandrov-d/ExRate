package com.example.exrate.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.exrate.data.ErrorCode;
import com.example.exrate.data.ServiceUnavailableException;
import com.example.exrate.data.rest.ExRateConversionsResponse;
import com.example.exrate.data.rest.ExRateRatesResponse;
import com.example.exrate.service.ExRateService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExRateController.class)
public class ExRateControllerTest {

  private final String USD = "USD";
  private final String EUR = "EUR";

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ExRateService exRateService;

  private ExRateConversionsResponse mockConversions;
  private ExRateRatesResponse ratesResponse;
  private final Set<String> availableCurrencies = Set.of("USD", "EUR", "GBP", "JPY");

  @BeforeEach
  void setUp() {
    ratesResponse = new ExRateRatesResponse();
    ratesResponse.setCurrency(USD);
    ratesResponse.getRates().put("EUR", new BigDecimal("0.853"));
    ratesResponse.getRates().put("GBP", new BigDecimal("0.7525"));

    mockConversions = new ExRateConversionsResponse();
    mockConversions.setFrom("USD");
    mockConversions.setAmount(new BigDecimal("100.00"));
    mockConversions.getConversions().put("EUR", new BigDecimal("85.0000"));
    mockConversions.getConversions().put("GBP", new BigDecimal("75.0000"));
  }

  @Test
  void getRates_OnlyFromParam_ReturnFullList() throws Exception {
    ratesResponse.setCurrency(USD);

    when(exRateService.getRates(eq(USD), isNull())).thenReturn(ratesResponse);
    when(exRateService.getAvailableCurrencies()).thenReturn(availableCurrencies);

    mockMvc.perform(get("/api/currency/rates")
            .param("from", USD)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currency").value("USD"))
        .andExpect(jsonPath("$.rates.*", Matchers.hasSize(2)))
        .andExpect(jsonPath("$.rates.EUR").value("0.853"))
        .andExpect(jsonPath("$.rates.GBP").value("0.7525"));
  }

  @Test
  void getRates_FromAndTo_Return1SizeList() throws Exception {
    ratesResponse.getRates().remove("GBP");
    when(exRateService.getRates(eq(USD), eq(EUR))).thenReturn(ratesResponse);
    when(exRateService.getAvailableCurrencies()).thenReturn(availableCurrencies);

    mockMvc.perform(get("/api/currency/rates")
            .param("from", USD)
            .param("to", EUR)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currency").value("USD"))
        .andExpect(jsonPath("$.rates.*", Matchers.hasSize(1)));
  }

  @Test
  void getRates_InvalidCurrency_BadRequest() throws Exception {
    mockMvc.perform(get("/api/currency/rates")
            .param("from", "")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getRates_CurrencyValidButNotAvailable_BadRequest() throws Exception {

    when(exRateService.getAvailableCurrencies()).thenReturn(availableCurrencies);

    mockMvc.perform(get("/api/currency/rates")
            .param("from", "NZD")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(exRateService).getAvailableCurrencies();
  }

  @Test
  void convert_ToCurrencyList_ReturnConversions() throws Exception {

    when(exRateService.getAvailableCurrencies()).thenReturn(availableCurrencies);
    when(exRateService.convert(
        eq(USD),
        eq(List.of("EUR", "GBP")),
        eq(new BigDecimal("100.00"))))
        .thenReturn(mockConversions);

    mockMvc.perform(get("/api/currency/convert")
            .param("from", USD)
            .param("amount", "100.00")
            .param("to", "EUR", "GBP")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.from").value("USD"))
        .andExpect(jsonPath("$.amount").value("100.0"))
        .andExpect(jsonPath("$.conversions.*", Matchers.hasSize(2)));
    verify(exRateService).getAvailableCurrencies();
  }

  @Test
  void convert_SingleCurrency_ReturnConversions() throws Exception {

    when(exRateService.getAvailableCurrencies()).thenReturn(availableCurrencies);
    when(exRateService.convert(eq(USD),eq(List.of(EUR)), eq(new BigDecimal("100.00"))))
        .thenReturn(mockConversions);

    mockMvc.perform(get("/api/currency/convert")
            .param("from", USD)
            .param("amount", "100.00")
            .param("to", "EUR")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.from").value("USD"))
        .andExpect(jsonPath("$.amount").value("100.0"))
        .andExpect(jsonPath("$.conversions.*", Matchers.hasSize(2)));
    verify(exRateService).getAvailableCurrencies();
  }

  @Test
  void convert_NoToParam_BadRequest() throws Exception {
    mockMvc.perform(get("/api/currency/convert")
            .param("from", USD)
            .param("amount", "100.00")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.msg").value("Required parameter 'to' is not present."));

    verifyNoInteractions(exRateService);
  }

  @Test
  void convert_InvalidAmount_BadRequest() throws Exception {
    mockMvc.perform(get("/api/currency/convert")
            .param("from", "USD")
            .param("amount", "invalid")
            .param("to", "EUR", "GBP")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.name()))
        .andExpect(jsonPath("$.msg").isNotEmpty());
    verifyNoInteractions(exRateService);
  }

  @Test
  void convert_ServiceUnavailableError_503Error() throws Exception {
    when(exRateService.getAvailableCurrencies()).thenReturn(availableCurrencies);
    when(exRateService.convert(any(), any(), any())).thenThrow(new ServiceUnavailableException("Service error"));

    mockMvc.perform(get("/api/currency/convert")
            .param("from", USD)
            .param("amount", "100.00")
            .param("to", "EUR", "GBP")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.msg").isNotEmpty());
    verify(exRateService).getAvailableCurrencies();
  }
}