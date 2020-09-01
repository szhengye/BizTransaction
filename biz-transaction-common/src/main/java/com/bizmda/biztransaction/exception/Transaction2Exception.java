package com.bizmda.biztransaction.exception;

public class Transaction2Exception extends  TransactionException {
    public final static int NO_MATCH_TRANSACTION_EXCEPTION_CODE = 1;
    public Transaction2Exception(int code) {
        super(code);
    }

    public Transaction2Exception(int code,String message, Throwable cause) {
        super(code,message, cause);
    }

    public Transaction2Exception(int code, Throwable cause) {
        super(code,cause);
    }
}
