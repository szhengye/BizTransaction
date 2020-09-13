package com.bizmda.biztransaction.service;

import cn.hutool.core.bean.BeanUtil;
import com.bizmda.biztransaction.annotation.QueueServiceAOP;
import com.bizmda.biztransaction.config.RabbitmqConfig;
import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import com.bizmda.biztransaction.util.SpringContextsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RabbitmqReceiverService {
    @Autowired
    private RabbitmqSenderService rabbitmqSenderService;

    /**
     * 同步确认服务重试队列的侦听消费处理
     * @param map 传入的重试消息
     */
    @RabbitListener(queues = RabbitmqConfig.syncConfirmServiceQueue, containerFactory = "multiListenerContainer")
    public void syncConfirmServiceListener(Map map) {
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

    /**
     * 异步通知服务队列的侦听消费处理
     * @param map 传入的异步通知服务消息
     */
    @RabbitListener(queues = RabbitmqConfig.QueueServiceQueue, containerFactory = "multiListenerContainer")
    public void queueServiceListener(Map map) {
//        log.info("***receive:{}", map);
        Object[] args = ((List)map.get("args")).toArray();
//        log.info("args:{},{}",args.length,args);
        Map transactionMap = (Map)map.get("transactionBean");
        String beanName = (String)transactionMap.get("beanName");
        AbstractTransaction transactionBean = (AbstractTransaction) SpringContextsUtil.getBean(beanName, AbstractTransaction.class);
        BeanUtil.copyProperties(transactionMap, transactionBean);
//        log.info("transactionBean:{}",transactionBean);
        String[] parameterTypes = ((List<String>) map.get("parameterTypes")).toArray(new String[0]);
//        log.info("parameterTypes:{},{}",parameterTypes.length,parameterTypes);
        String methodName = (String)map.get("methodName");
//        log.info("methodName:{}",methodName);
        List<Class> classArray = new ArrayList<Class>();

        for(int i = 0;i<args.length;i++) {
//            log.info("arg:{}",args[i] instanceof Map,args[i] instanceof List);
            try {
                classArray.add(Class.forName(parameterTypes[i]));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }
            if (args[i] instanceof Map || args[i] instanceof List) {
                Object o = null;
                try {
                    o = Class.forName(parameterTypes[i]).newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    return;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
                BeanUtil.copyProperties(args[i],o);
                args[i] = o;
            }
        }
        Class[] classes = classArray.toArray(new Class[0]);
        Method method = null;
        try {
            method = transactionBean.getClass().getMethod(methodName, classes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }
        QueueServiceAOP.queueServiceListener.set(true);
        try {
            method.invoke(transactionBean,args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        finally {
            QueueServiceAOP.queueServiceListener.remove();
        }
    }
}
