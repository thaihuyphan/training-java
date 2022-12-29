package com.kegmil.example.pcbook.service;

public class NotFoundExistException extends RuntimeException{

    public NotFoundExistException(String message) {
        super(message);
    }
}
