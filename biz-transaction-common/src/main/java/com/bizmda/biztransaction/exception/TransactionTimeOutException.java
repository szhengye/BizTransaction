package com.bizmda.biztransaction.exception;

/**
 * 交易超时异常
 */
public class TransactionTimeOutException extends TransactionException {

    public TransactionTimeOutException(String message, Throwable cause) {
        super(TransactionException.TIMEOUT_EXCEPTION_CODE, message, cause);
    }

    public TransactionTimeOutException(Throwable cause) {
        super(TransactionException.TIMEOUT_EXCEPTION_CODE, cause);
    }

//    public TransactionTimeOutException(int code) {
//        super(code);
//    }

    public TransactionTimeOutException() {
        super(TransactionException.TIMEOUT_EXCEPTION_CODE);
    }
}
