package com.bizmda.biztransaction.exception;

/**
 * 统一封装的交易异常公共父类
 */
public class BizTranException extends  Exception {
    /**
     * 外部服务超时
     */
    public final static int TIMEOUT_EXCEPTION_CODE = 1;
    /**
     * 外部服务响应错误
     */
    public final static int RESP_ERROR_EXCEPTION_CODE = 2;
    /**
     * 事务主动回滚
     */
    public final static int ROLLBACK_EXCEPTION_CODE = 3;
    /**
     * 超时重试达到最大重试资料
     */
    public final static int MAX_CONFIRM_EXCEPTION_CODE = 4;
    /**
     * 找不到原交易数据
     */
    public final static int NO_MATCH_TRANSACTION_EXCEPTION_CODE = 5;

    private int code;

    public BizTranException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public BizTranException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public BizTranException(int code) {
        super();
        this.code = code;
    }

    public BizTranException() {
        super();
    }

    public int getCode() {
        return code;
    }
}
