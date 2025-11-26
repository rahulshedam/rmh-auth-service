package com.rmh.auth.exception;

public class PasswordSameAsCurrentException extends RuntimeException {
    public PasswordSameAsCurrentException() { super(); }
    public PasswordSameAsCurrentException(String message) { super(message); }
    public PasswordSameAsCurrentException(String message, Throwable cause) { super(message, cause); }
}

