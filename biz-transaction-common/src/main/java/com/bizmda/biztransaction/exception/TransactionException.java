package com.bizmda.biztransaction.exception;

public class TransactionException extends  Exception {
    private int code;

    public TransactionException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public TransactionException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public TransactionException(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
