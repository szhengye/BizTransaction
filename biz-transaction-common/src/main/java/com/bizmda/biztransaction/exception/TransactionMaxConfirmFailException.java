package com.bizmda.biztransaction.exception;

public class TransactionMaxConfirmFailException extends  TransactionException {
    public TransactionMaxConfirmFailException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public TransactionMaxConfirmFailException(int code, Throwable cause) {
        super(code, cause);
    }

    public TransactionMaxConfirmFailException(int code) {
        super(code);
    }

    public TransactionMaxConfirmFailException() {
        super();
    }
}
