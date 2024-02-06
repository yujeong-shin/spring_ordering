package com.encore.ordering.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ErrorResponseDto {
    public static ResponseEntity<Map<String, Object>> makeMessage(HttpStatus status, String message){
        Map<String, Object> body = new HashMap<>();
        body.put("status", Integer.toString(status.value())); //responseBody에 출력되도록 넣기
        // 프론트엔드에서 response.data. 형식으로 받아주기 위해 변수명 변경
        body.put("status_message", status.getReasonPhrase());
        body.put("error_message", message);
        return new ResponseEntity<>(body, status); //status에 담긴 값은 header로 나가고, body에 담긴 Map은 json 형태로 나감.
    }
}
