package com.bizmda.biztransaction.service;

import cn.hutool.core.bean.BeanUtil;
import com.bizmda.biztransaction.annotation.SyncServiceAOP;
import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * RabbitMQ接收消息服务
 **/
@Slf4j
@Service
public class RabbitmqSyncServiceReceiverService {
    @Autowired
    private RabbitmqSenderService rabbitmqSenderService;

    /**
     * ttl消息
     *
     * @param map
     */
    @RabbitListener(queues = {"dead.real.queue"}, containerFactory = "multiListenerContainer")
    public void syncServiceListener(Map map) {
//        log.info("***receive:{}", map);
        Map transactionMap = (Map)map.get("transactionBean");
        String beanName = (String)transactionMap.get("beanName");
        AbstractTransaction transactionBean = (AbstractTransaction) SpringContextsUtil.getBean(beanName, AbstractTransaction.class);
        BeanUtil.copyProperties(transactionMap, transactionBean);
//        log.info("transactionBean:{}",transactionBean);
        String confirmMethodName = (String)map.get("confirmMethod");
        String commitMethodName = (String)map.get("commitMethod");
        String rollbackMethodName = (String)map.get("rollbackMethod");
        Method confirmMethod = null;
        Method commitMethod = null;
        Method rollbackMethod = null;
        try {
            confirmMethod = transactionBean.getClass().getMethod(confirmMethodName);
            commitMethod = transactionBean.getClass().getMethod(commitMethodName);
            rollbackMethod = transactionBean.getClass().getMethod(rollbackMethodName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }

        try {
            Boolean confirmSuccess = (Boolean)confirmMethod.invoke(transactionBean);
            if (confirmSuccess) {
                commitMethod.invoke(transactionBean);
            } else {
                rollbackMethod.invoke(transactionBean);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            if (e.getTargetException().getClass().equals(TransactionTimeOutException.class)) {
                transactionBean.setConfirmTimes(transactionBean.getConfirmTimes() + 1);
                if (transactionBean.getConfirmTimes() >= RabbitmqSenderService.expirationArray.length) {
                    transactionBean.abortTransaction(
                            new TransactionException(TransactionException.MAX_CONFIRM_EXCEPTION_CODE));
                    return;
                }
                rabbitmqSenderService.sendSyncService(transactionBean,confirmMethodName,commitMethodName,rollbackMethodName);
            }
            else {
                e.printStackTrace();
            }
        }

    }
}












