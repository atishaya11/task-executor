package com.dscjss.taskexecutor.util;

public enum Status {
    TIME_LIMIT_EXCEEDED(0),
    COMPILATION_ERROR(1),
    RUNTIME_ERROR(2),
    INTERNAL_ERROR(3),
    EXECUTED(4);
    private final int code;

    Status(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
