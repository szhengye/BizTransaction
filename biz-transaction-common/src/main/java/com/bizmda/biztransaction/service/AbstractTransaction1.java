package com.bizmda.biztransaction.service;

import com.bizmda.biztransaction.exception.TransactionMaxConfirmFailException;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

public abstract class AbstractTransaction1 implements BeanNameAware {
    private Map transactionContext;

    @Autowired
    private RabbitSenderService rabbitSenderService ;

    // 保存部署该Bean时指定的id属性
    private String beanName;
    public void setBeanName(String name)
    {
        this.beanName = name;
    }

    @Transactional
    public void doService(Object msg) {
        this.doInnerService1(msg);
        try {
            if (this.doOuterService(msg)) {
                this.doInnerService2(msg);
            }
            else {
                this.cancelInnerService1(msg);
            }
        } catch (TransactionTimeOutException e) {
            try {
                rabbitSenderService.sendTTLExpireMsg(1, this.beanName, 0, msg, this.transactionContext);
            } catch (TransactionMaxConfirmFailException transactionMaxConfirmFailException) {
                transactionMaxConfirmFailException.printStackTrace();
            }
        }
    }

    public void setTransactionContext(Map transactionContext) {
        this.transactionContext = transactionContext;
    }

    public abstract void doInnerService1(Object msg);
    public abstract boolean doOuterService(Object msg) throws TransactionTimeOutException;
    public abstract void doInnerService2(Object msg);
    public abstract boolean confirmOuterService(Object msg) throws TransactionTimeOutException;
    public abstract void cancelInnerService1(Object msg);
}
