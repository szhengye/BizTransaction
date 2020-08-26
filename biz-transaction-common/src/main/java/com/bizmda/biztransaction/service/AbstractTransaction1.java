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
    private int confirmTimes;

    public int getConfirmTimes() {
        return confirmTimes;
    }

    public void setConfirmTimes(int confirmTimes) {
        this.confirmTimes = confirmTimes;
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
        this.confirmTimes = 0;
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

    // 实现第1步内部服务的处理逻辑
    public abstract void doInnerService1(Object inParams);
    // 实现第2步调用外部第三方应用的处理逻辑，如果响应超时，应抛出TransactionTimeoutException，Biz-Transaction会根据超时重试机制自动重发，具体实现是通过RabbitMQ的延迟队列来实现的。
    public abstract boolean doOuterService() throws TransactionTimeOutException;
    // 实现第2步内部服务的处理逻辑
    public abstract Object doInnerService2();
    // 实现第2步调用外部第三方应用超时无响应后，后续向第三方应用发起交易确认的处理逻辑，如果超时，应抛出TransactionTimeoutException，Biz-Transaction会根据超时重试机制自动重发，具体实现是通过RabbitMQ的延迟队列来实现的。
    public abstract boolean confirmOuterService() throws TransactionTimeOutException;
    // 针对第1步内部服务的补偿服务处理逻辑。
    public abstract void cancelInnerService1();

    public void abortTransaction(Throwable e) {
        log.info("abortTransaction:{}",e.getMessage());
    }
}
