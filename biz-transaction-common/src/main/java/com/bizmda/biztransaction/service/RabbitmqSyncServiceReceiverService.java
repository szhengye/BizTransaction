package com.bizmda.biztransaction.service;

import cn.hutool.core.bean.BeanUtil;
import com.bizmda.biztransaction.annotation.SyncServiceAOP;
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
    public void consumeExpirMsg(Map map) {
        log.info("***receive:{}", map);
//        Object[] args = ((List)map.get("args")).toArray();
//        log.info("args:{},{},{}",args.length,args[0],args[1]);
        Map transactionMap = (Map)map.get("transactionBean");
        String beanName = (String)transactionMap.get("beanName");
        AbstractTransaction transactionBean = (AbstractTransaction) SpringContextsUtil.getBean(beanName, AbstractTransaction.class);
        BeanUtil.copyProperties(transactionMap, transactionBean);
        log.info("transactionBean:{}",transactionBean);
        String confirmMethodName = (String)map.get("confirmMethod");
        String commitMethodName = (String)map.get("commitMethod");
        String rollbackMethodName = (String)map.get("rollbackMethod");
        Class[] classes = new Class[0];
        Method confirmMethod = null;
        Method commitMethod = null;
        Method rollbackMethod = null;
        try {
            confirmMethod = transactionBean.getClass().getMethod(confirmMethodName);
//            , classes);
            commitMethod = transactionBean.getClass().getMethod(commitMethodName);
//            , classes);
            rollbackMethod = transactionBean.getClass().getMethod(rollbackMethodName);
//            , classes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }

        AbstractTransaction transaction1 = (AbstractTransaction1) SpringContextsUtil.getBean(beanName, AbstractTransaction1.class);
        BeanUtil.copyProperties(map, transaction1);
        log.info("***receive:transactionBean:{},{}", transaction1.getClass().getName(), transaction1);
        if (transaction1.getConfirmTimes() >= RabbitmqSenderService.expirationArray.length - 2) {
            transaction1.abortTransaction(new TransactionTimeOutException());
            return;
        }
        SyncServiceAOP.syncServiceListener.set(true);

        try {
            Boolean confirmSuccess = (Boolean)confirmMethod.invoke(transaction1);
            if (confirmSuccess) {
                commitMethod.invoke(transaction1);
//                transaction1.doInnerService2();
            } else {
                rollbackMethod.invoke(transaction1);
//                transaction1.cancelInnerService1();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            if (e.getTargetException().getClass().equals(TransactionTimeOutException.class)) {
                transaction1.setConfirmTimes(transaction1.getConfirmTimes() + 1);
                rabbitmqSenderService.sendSyncService(transaction1,confirmMethodName,commitMethodName,rollbackMethodName);
            }
            else {
                e.printStackTrace();
            }
        }
        finally {
            SyncServiceAOP.syncServiceListener.remove();
        }
    }
}












