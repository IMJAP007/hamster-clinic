package com.academy.hamster_clinic.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(String message){
        super(message);
    }
}
