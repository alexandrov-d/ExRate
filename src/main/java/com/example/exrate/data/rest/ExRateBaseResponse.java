package com.example.exrate.data.rest;

import com.example.exrate.data.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExRateBaseResponse {
  @Schema(description = "Error codes if something wrong, OK otherwise")
  private ErrorCode code = ErrorCode.OK;
  @Schema(description = "Error msg or null on success")
  private String msg;
}
