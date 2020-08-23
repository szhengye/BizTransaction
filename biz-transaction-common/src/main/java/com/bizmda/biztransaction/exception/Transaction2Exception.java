package com.bizmda.biztransaction.exception;

public class Transaction2Exception extends  Exception {
    public final static int NO_MATCH_TRANSACTION_EXCEPTION_CODE = 1;

    private int code;

    public Transaction2Exception(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Transaction2Exception(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public Transaction2Exception(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
