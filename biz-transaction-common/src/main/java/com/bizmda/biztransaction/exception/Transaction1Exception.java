package com.bizmda.biztransaction.exception;

public class Transaction1Exception extends  Exception {
    public final static int OUTER_SERVICE_TIMEOUT_EXCEPTION_CODE = 1;
    public final static int CANCEL_SERVICE_EXCEPTION_CODE = 2;
    public final static int MAX_CONFIRM_EXCEPTION_CODE = 3;

    private int code;

    public Transaction1Exception(int code,String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Transaction1Exception(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public Transaction1Exception(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
