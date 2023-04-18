package com.example.jwttest.global.exception.model;

public record ExceptionResponseEntity(String message) {

    public static ExceptionResponseEntity of(final Throwable exception) {
        return new ExceptionResponseEntity(exception.getMessage());
    }

}
