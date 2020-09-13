package com.bizmda.biztransaction.service;

import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 同步超时确认交易处理机制的抽象父类
 */
@Slf4j
public abstract class AbstractBizTran1 extends AbstractBizTran {
    @Autowired
    private RabbitmqSenderService rabbitmqSenderService;

    /**
     * 服务统一调用入口
     * @param inParams 服务调用参数
     * @return 服务返回结果
     * @throws TransactionException
     */
    public Object doService(Object inParams) throws TransactionException {
        this.setConfirmTimes(0);
        this.beforeSyncService(inParams);
        try {
            if (this.doSyncService()) {
                return this.afterSyncService();
            } else {
                this.rollbackService();
                throw new TransactionException(TransactionException.ROLLBACK_EXCEPTION_CODE);
            }
        } catch (TransactionTimeOutException e) {
            rabbitmqSenderService.sendSyncConfirmService(this, "confirmSyncService", "afterSyncService", "rollbackService");
            throw new TransactionException(TransactionException.TIMEOUT_EXCEPTION_CODE, e);
        }
    }

    /**
     * 实现第1步内部服务的处理逻辑
     * @param inParams 服务调用参数
     */
    public abstract void beforeSyncService(Object inParams);

    /**
     * 实现第2步调用外部第三方应用的处理逻辑，如果响应超时，应抛出TransactionTimeoutException，Biz-Transaction会根据超时重试机制自动重发，具体实现是通过RabbitMQ的延迟队列来实现的。
     * @return 返回是否处理成功
     * @throws TransactionTimeOutException
     */
    public abstract boolean doSyncService() throws TransactionTimeOutException;

    /**
     * 实现第2步内部服务的处理逻辑
     * @return 返回处理结果
     */
    public abstract Object afterSyncService();

    /**
     * 实现第2步调用外部第三方应用超时无响应后，后续向第三方应用发起交易确认的处理逻辑，如果超时，应抛出TransactionTimeoutException，Biz-Transaction会根据超时重试机制自动重发，具体实现是通过RabbitMQ的延迟队列来实现的。
     * @return 返回确认重试是否处理成功
     * @throws TransactionTimeOutException
     */
    public abstract boolean confirmSyncService() throws TransactionTimeOutException;

    /**
     * 针对第1步内部服务的补偿服务处理逻辑。
     */
    public abstract void rollbackService();
}


