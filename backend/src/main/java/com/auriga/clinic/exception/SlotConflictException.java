package com.auriga.clinic.exception;

public class SlotConflictException extends RuntimeException {
    public SlotConflictException(String message) {
        super(message);
    }
}