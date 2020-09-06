package com.bizmda.biztransaction.exception;

public class TransactionTimeOutException extends TransactionException {

    public TransactionTimeOutException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public TransactionTimeOutException(int code, Throwable cause) {
        super(code, cause);
    }

    public TransactionTimeOutException(int code) {
        super(code);
    }

    public TransactionTimeOutException() {
        super();
    }
}
