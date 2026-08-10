package com.likelion14.runcovery.exception;

public class BodyPartNotFoundException extends RuntimeException {
    public BodyPartNotFoundException() {
        super("해당 신체 부위가 존재하지 않습니다");
    }
}
