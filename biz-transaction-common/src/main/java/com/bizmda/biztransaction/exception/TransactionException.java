package com.bizmda.biztransaction.exception;

public class TransactionException extends  Exception {
    public final static int OUTER_SERVICE_TIMEOUT_EXCEPTION_CODE = 1;
    public final static int CANCEL_SERVICE_EXCEPTION_CODE = 2;
    public final static int MAX_CONFIRM_EXCEPTION_CODE = 3;
    public final static int NO_MATCH_TRANSACTION_EXCEPTION_CODE = 4;

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
        super();
        this.code = code;
    }

    public TransactionException() {
        super();
    }

    public int getCode() {
        return code;
    }
}
