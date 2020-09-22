package com.bizmda.biztransaction.exception;

/**
 * 交易超时异常
 */
public class BizTranRespErrorException extends BizTranException {

    public BizTranRespErrorException(String message, Throwable cause) {
        super(BizTranException.ROLLBACK_EXCEPTION_CODE, message, cause);
    }

    public BizTranRespErrorException(Throwable cause) {
        super(BizTranException.ROLLBACK_EXCEPTION_CODE, cause);
    }

//    public TransactionTimeOutException(int code) {
//        super(code);
//    }

    public BizTranRespErrorException() {
        super(BizTranException.ROLLBACK_EXCEPTION_CODE);
    }
}
