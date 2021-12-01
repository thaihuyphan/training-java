package com.kegmil.example.pcbook.service;

public class NotExistException extends RuntimeException {
  public NotExistException(String message) {
    super(message);
  }
}
