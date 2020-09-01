package com.bizmda.biztransaction.exception;

public class Transaction1Exception extends TransactionException {
    public final static int OUTER_SERVICE_TIMEOUT_EXCEPTION_CODE = 1;
    public final static int CANCEL_SERVICE_EXCEPTION_CODE = 2;
    public final static int MAX_CONFIRM_EXCEPTION_CODE = 3;

    public Transaction1Exception(int code) {
        super(code);
    }

    public Transaction1Exception(int code,String message, Throwable cause) {
        super(code,message, cause);
    }

    public Transaction1Exception(int code, Throwable cause) {
        super(code,cause);
    }

}
