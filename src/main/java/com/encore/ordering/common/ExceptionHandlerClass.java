package com.encore.ordering.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class ExceptionHandlerClass {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> entityNotFoundHandler(EntityNotFoundException e){
        log.error("Handler EntityNotFoundException message: " + e.getMessage());
        return ErrorResponseDto.makeMessage(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>>  IllegalArguHandler(IllegalArgumentException e){
        log.error("Handler EntityNotFoundException message: " + e.getMessage());
        return ErrorResponseDto.makeMessage(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}