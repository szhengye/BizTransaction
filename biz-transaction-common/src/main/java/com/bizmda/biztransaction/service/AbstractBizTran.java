package com.bizmda.biztransaction.service;

import com.bizmda.biztransaction.exception.TransactionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanNameAware;

/**
 * 所有交易处理类的抽象父类
 */
@Slf4j
public abstract class AbstractBizTran implements BeanNameAware {
    /**
     * 容器中的Bean名称
     */
    private String beanName;

    @Override
    public void setBeanName(String name)
    {
        this.beanName = name;
    }

    public String getBeanName() {
        return beanName;
    }

    /**
     * 记录确认重发次数
     */
    private int confirmTimes;

    public int getConfirmTimes() {
        return confirmTimes;
    }

    public void setConfirmTimes(int confirmTimes) {
        this.confirmTimes = confirmTimes;
    }

    /**
     * 服务调用入口
     * @param inParams 服务调用参数
     * @return 服务返回结果
     * @throws TransactionException
     */
    public abstract Object doService(Object inParams) throws TransactionException;

    /**
     * 交易处理异常中止时统一调用的方法
     * @param e
     */
    public void abortTransaction(Throwable e) {
        log.info("abortTransaction:{}",e.getMessage());
    }

}