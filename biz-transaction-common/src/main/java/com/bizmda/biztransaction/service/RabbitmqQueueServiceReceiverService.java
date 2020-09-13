package com.bizmda.biztransaction.service;

import cn.hutool.core.bean.BeanUtil;
import com.bizmda.biztransaction.annotation.QueueServiceAOP;
import com.bizmda.biztransaction.config.RabbitmqConfig;
import com.bizmda.biztransaction.exception.TransactionMaxConfirmFailException;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import com.bizmda.biztransaction.service.AbstractTransaction;
import com.bizmda.biztransaction.service.SpringContextsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RabbitMQ接收消息服务
 **/
@Slf4j
@Service
public class RabbitmqQueueServiceReceiverService {

    @RabbitListener(queues = RabbitmqConfig.QueueServiceQueue, containerFactory = "multiListenerContainer")
//    @RabbitListener(queues = RabbitmqConfig.QueueServiceQueue)
    public void consume(Map map) {
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












