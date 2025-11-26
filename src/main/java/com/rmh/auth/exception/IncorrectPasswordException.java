package com.rmh.auth.exception;

public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException() { super(); }
    public IncorrectPasswordException(String message) { super(message); }
    public IncorrectPasswordException(String message, Throwable cause) { super(message, cause); }
}

