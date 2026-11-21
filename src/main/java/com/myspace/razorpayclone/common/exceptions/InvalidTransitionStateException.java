package com.myspace.razorpayclone.common.exceptions;

import lombok.Getter;

@Getter
public class InvalidTransitionStateException extends RuntimeException {
    private final String fromState;
    private final String event;

    public  InvalidTransitionStateException(String fromState, String event) {
        super("Invalid transition state " + fromState + " to " + event);
        this.fromState = fromState;
        this.event = event;
    }

}
