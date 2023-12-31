package com.serendipity.exception;

import lombok.Data;

@Data
public class CheckException extends RuntimeException {
    /**
     * 出错字段的名字
     */
    private String fieldName;
    /**
     * 出错字段的值
     */
    private String fieldValue;

    public CheckException() {
        super();
    }

    public CheckException(String message) {
        super(message);
    }

    public CheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public CheckException(Throwable cause) {
        super(cause);
    }

    protected CheckException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CheckException(String fieldName, String fieldValue) {
        super();
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
