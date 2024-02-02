package com.encore.ordering.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class CommonResponse {
    // 단건 create 했을 때, "ok" 주기 싫으니까 공통화
    private HttpStatus status;
    private String message;
    private Object result;
}
