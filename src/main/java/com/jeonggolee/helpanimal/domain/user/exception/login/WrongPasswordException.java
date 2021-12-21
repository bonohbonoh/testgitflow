package com.jeonggolee.helpanimal.domain.user.exception.login;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Wrong Password Input")
public class WrongPasswordException extends RuntimeException{
    public WrongPasswordException(String msg) {
        super(msg);
    }
}
