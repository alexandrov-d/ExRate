package com.example.exrate.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.exrate.data.rest.ExRateRatesResponse;
import com.example.exrate.service.ExRateService;
import java.math.BigDecimal;
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

  private ExRateRatesResponse ratesResponse;

  @BeforeEach
  void setUp() {
    ratesResponse = new ExRateRatesResponse();
    ratesResponse.setCurrency(USD);
    ratesResponse.getRates().put("EUR", new BigDecimal("0.853"));
    ratesResponse.getRates().put("GBP", new BigDecimal("0.7525"));
  }

  @Test
  void getRates_OnlyFromParam_ReturnFullList() throws Exception {
    ratesResponse.setCurrency(USD);

    when(exRateService.getRates(eq(USD), isNull())).thenReturn(ratesResponse);

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
}