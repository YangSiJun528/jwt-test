package com.example.jwttest.global.exception;

import com.example.jwttest.global.exception.error.ExpectedException;
import com.example.jwttest.global.exception.model.ExceptionResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ExpectedException.class)
    private ResponseEntity<ExceptionResponseEntity> expectedException(ExpectedException ex) {
        return ResponseEntity.status(ex.getStatusCode().value())
                .body(ExceptionResponseEntity.of(ex));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponseEntity> unExpectedException(RuntimeException ex) {
        log.error("unExpectedException : ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(new ExceptionResponseEntity("internal server error has occurred"));
    }

    // 404 에러 처리 - helloGSM 참고

}
