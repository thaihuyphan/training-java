package com.kegmil.example.pcbook.service;

import javax.management.RuntimeErrorException;

public class NotExistException extends RuntimeException {
    public NotExistException(String message) {super(message);}
}
