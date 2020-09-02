package com.bizmda.biztransaction.service;

import com.bizmda.biztransaction.exception.Transaction1Exception;
import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanNameAware;

@Slf4j
public abstract class AbstractTransaction implements BeanNameAware {
    // 保存部署该Bean时指定的id属性
    private String beanName;
    public void setBeanName(String name)
    {
        this.beanName = name;
    }

    public String getBeanName() {
        return beanName;
    }

    private int confirmTimes;

    public int getConfirmTimes() {
        return confirmTimes;
    }

    public void setConfirmTimes(int confirmTimes) {
        this.confirmTimes = confirmTimes;
    }

    public abstract Object doService(Object inParams) throws TransactionException;
    public void abortTransaction(Throwable e) {
        log.info("abortTransaction:{}",e.getMessage());
    }

}
