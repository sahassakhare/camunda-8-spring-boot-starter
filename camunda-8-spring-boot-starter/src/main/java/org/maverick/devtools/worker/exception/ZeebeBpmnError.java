package org.maverick.devtools.worker.exception;

import lombok.Getter;

@Getter
public class ZeebeBpmnError extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;

    public ZeebeBpmnError(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
