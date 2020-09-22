package com.bizmda.biztransaction.exception;

/**
 * 交易超时异常
 */
public class BizTranTimeOutException extends BizTranException {

    public BizTranTimeOutException(String message, Throwable cause) {
        super(BizTranException.TIMEOUT_EXCEPTION_CODE, message, cause);
    }

    public BizTranTimeOutException(Throwable cause) {
        super(BizTranException.TIMEOUT_EXCEPTION_CODE, cause);
    }

//    public TransactionTimeOutException(int code) {
//        super(code);
//    }

    public BizTranTimeOutException() {
        super(BizTranException.TIMEOUT_EXCEPTION_CODE);
    }
}
