package com.bizmda.biztransaction.service;

import com.bizmda.biztransaction.exception.Transaction1Exception;
import com.bizmda.biztransaction.exception.TransactionMaxConfirmFailException;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractTransaction1 implements BeanNameAware {
//    private Map transactionContext;

    private int confirmStep;

    public int getConfirmStep() {
        return confirmStep;
    }

    public void setConfirmStep(int confirmStep) {
        this.confirmStep = confirmStep;
    }

    @Autowired
    private RabbitSenderService rabbitSenderService ;

    // 保存部署该Bean时指定的id属性
    private String beanName;
    public void setBeanName(String name)
    {
        this.beanName = name;
    }

    public String getBeanName() {
        return beanName;
    }

    public Object doService(Object inParams) throws Transaction1Exception {
        this.confirmStep = 1;
        this.doInnerService1(inParams);
        try {
            if (this.doOuterService()) {
                return this.doInnerService2();
            }
            else {
                this.cancelInnerService1();
                throw new Transaction1Exception(Transaction1Exception.CANCEL_SERVICE_EXCEPTION_CODE);
            }
        } catch (TransactionTimeOutException e) {
            try {
                rabbitSenderService.sendOuterServiceConfirmMsg(this);
            } catch (TransactionMaxConfirmFailException transactionMaxConfirmFailException) {
                transactionMaxConfirmFailException.printStackTrace();
                throw new Transaction1Exception(Transaction1Exception.MAX_CONFIRM_EXCEPTION_CODE,e);
            }
            throw new Transaction1Exception(Transaction1Exception.OUTER_SERVICE_TIMEOUT_EXCEPTION_CODE,e);
        }
    }

//    public void setTransactionContext(Map transactionContext) {
//        this.transactionContext = transactionContext;
//    }

    public abstract void doInnerService1(Object msg);
    public abstract boolean doOuterService() throws TransactionTimeOutException;
    public abstract Object doInnerService2();
    public abstract boolean confirmOuterService() throws TransactionTimeOutException;
    public abstract void cancelInnerService1();

    public void abortTransaction(Throwable e) {
        log.info("abortTransaction:{}",e.getMessage());
    }
}
