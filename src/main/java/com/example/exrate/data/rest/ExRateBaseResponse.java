package com.example.exrate.data.rest;

import com.example.exrate.data.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExRateBaseResponse {
  private ErrorCode code = ErrorCode.OK;
  private String msg;
}
