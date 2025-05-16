package com.nhnacademy.gateway.exception;

public class TokenBlacklistedException extends RuntimeException {
    public TokenBlacklistedException() {
        super("This token is blacklisted.");
    }
}
