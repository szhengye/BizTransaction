package com.bizmda.biztransaction.service;

import com.bizmda.biztransaction.exception.BizTranException;
import com.bizmda.biztransaction.util.BizTranContext;
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
     * 交易类上下文环境
     */
    private static final ThreadLocal<BizTranContext> bizTranContextThreadLocal = new ThreadLocal<BizTranContext>();

    public void setTranContext(BizTranContext bizTranContext) {
        bizTranContextThreadLocal.set(bizTranContext);
    }

    public BizTranContext getTranContext() {
        BizTranContext bizTranContext = bizTranContextThreadLocal.get();
        if (bizTranContext == null) {
            this.setTranContext(new BizTranContext());
            bizTranContext = bizTranContextThreadLocal.get();
        }
        return bizTranContext;
    }

    /**
     * 记录确认重发次数
     */
    private static final ThreadLocal<Integer> confirmTimesThreadLocal = new ThreadLocal<Integer>();

    public int getConfirmTimes() {
        Integer confirmTimes = confirmTimesThreadLocal.get();
        if (confirmTimes == null) {
            this.setConfirmTimes(0);
            confirmTimes = confirmTimesThreadLocal.get();
        }
        return confirmTimes;
    }

    public void setConfirmTimes(int confirmTimes) {
        confirmTimesThreadLocal.set(confirmTimes);
    }

    /**
     * 交易处理异常中止时统一调用的方法
     * @param e
     */
    public void abortTransaction(Throwable e) {
        log.info("abortTransaction:{}",e.getMessage());
    }

}
