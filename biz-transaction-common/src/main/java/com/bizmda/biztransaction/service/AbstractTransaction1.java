package com.bizmda.biztransaction.service;

import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractTransaction1 extends AbstractTransaction {
    @Autowired
    private RabbitmqSenderService rabbitmqSenderService;

    public Object doService(Object inParams) throws TransactionException {
        this.setConfirmTimes(0);
        this.beforeSyncService(inParams);
        try {
            if (this.doSyncService()) {
                return this.afterSyncService();
            } else {
                this.rollbackService();
                throw new TransactionException(TransactionException.CANCEL_SERVICE_EXCEPTION_CODE);
            }
        } catch (TransactionTimeOutException e) {
            rabbitmqSenderService.sendSyncService(this, "confirmSyncService", "afterSyncService", "rollbackService");
            throw new TransactionException(TransactionException.OUTER_SERVICE_TIMEOUT_EXCEPTION_CODE, e);
        }
    }

//    public void setTransactionContext(Map transactionContext) {
//        this.transactionContext = transactionContext;
//    }

    // 实现第1步内部服务的处理逻辑
    public abstract void beforeSyncService(Object inParams);

    // 实现第2步调用外部第三方应用的处理逻辑，如果响应超时，应抛出TransactionTimeoutException，Biz-Transaction会根据超时重试机制自动重发，具体实现是通过RabbitMQ的延迟队列来实现的。
    public abstract boolean doSyncService() throws TransactionTimeOutException;

    // 实现第2步内部服务的处理逻辑
    public abstract Object afterSyncService();

    // 实现第2步调用外部第三方应用超时无响应后，后续向第三方应用发起交易确认的处理逻辑，如果超时，应抛出TransactionTimeoutException，Biz-Transaction会根据超时重试机制自动重发，具体实现是通过RabbitMQ的延迟队列来实现的。
    public abstract boolean confirmSyncService() throws TransactionTimeOutException;

    // 针对第1步内部服务的补偿服务处理逻辑。
    public abstract void rollbackService();
}


