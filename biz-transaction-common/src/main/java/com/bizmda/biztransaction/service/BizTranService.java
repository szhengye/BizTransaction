package com.bizmda.biztransaction.service;

import cn.hutool.core.bean.BeanUtil;
import com.bizmda.biztransaction.annotation.QueueServiceAOP;
import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import com.bizmda.biztransaction.util.SpringContextsUtil;
import com.open.capacity.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 中间件公共的调用方法，封装在服务中
 */
@Slf4j
@Service
public class BizTranService {
    @Autowired
    private RedisUtil redisUtil ;
    @Autowired
    private RabbitmqSenderService rabbitmqSenderService;

    /**
     * 异步服务回调方法
     * @param outerId 服务id
     * @param transactionKey 交易唯一键
     * @param inParams 回调输入参数
     * @return
     * @throws TransactionException
     */
    public Object asyncServiceCallback(String outerId, String transactionKey, Object inParams) throws TransactionException {
        String key = "biz:asyncservice:" + outerId + ":" + transactionKey;
        String preKey = "biz:pre_asyncservice:" + outerId + ":" + transactionKey;
        Map context = (Map)this.redisUtil.get(key);
        if (context == null) {
            throw new TransactionException(TransactionException.NO_MATCH_TRANSACTION_EXCEPTION_CODE);
        }
        this.redisUtil.del(key);
        this.redisUtil.del(preKey);
        Map transactionMap = (Map)context.get("transactionBean");
        String callbackMethodName = (String)context.get("callbackMethod");

        String beanName = (String)transactionMap.get("beanName");
        AbstractBizTran bizTran = (AbstractBizTran) SpringContextsUtil.getBean(beanName, AbstractBizTran.class);
        BeanUtil.copyProperties(transactionMap, bizTran);

        Method callbackMethod = null;
        try {
            callbackMethod = bizTran.getClass().getMethod(callbackMethodName,Object.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }

        try {
            return callbackMethod.invoke(bizTran,inParams);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 异步服务回调超时
     * @param preKey 超时的Redis key
     * @throws TransactionException
     */
    public void asyncServiceTimeout(String preKey) throws TransactionException {
        String[] a = preKey.split(":");
        String key = "biz:asyncservice:" + a[2] + ":" + a[3];
        Map context = (Map)this.redisUtil.get(key);
        if (context == null) {
            throw new TransactionException(TransactionException.NO_MATCH_TRANSACTION_EXCEPTION_CODE);
        }
        this.redisUtil.del(key);
        Map transactionMap = (Map)context.get("transactionBean");
        String timeoutMethodName = (String)context.get("timeoutMethod");


        String beanName = (String)transactionMap.get("beanName");
        AbstractBizTran transaction2 = (AbstractBizTran) SpringContextsUtil.getBean(beanName, AbstractBizTran.class);
        BeanUtil.copyProperties(transactionMap, transaction2);

        Method timeoutMethod = null;
        try {
            timeoutMethod = transaction2.getClass().getMethod(timeoutMethodName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }

        try {
            timeoutMethod.invoke(transaction2);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 同步确认消息接收处理
     * @param map 消息体
     */
    public void syncConfirmServiceReceive(Map map) {
        Map transactionMap = (Map)map.get("transactionBean");
        String beanName = (String)transactionMap.get("beanName");
        AbstractBizTran transactionBean = (AbstractBizTran) SpringContextsUtil.getBean(beanName, AbstractBizTran.class);
        BeanUtil.copyProperties(transactionMap, transactionBean);
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
                rabbitmqSenderService.sendSyncConfirmService(transactionBean,confirmMethodName,commitMethodName,rollbackMethodName);
            }
            else {
                e.printStackTrace();
            }
        }
    }

    /**
     * 异步服务消息接收处理
     * @param map 消息体
     */
    public void queueServiceService(Map map) {
        Object[] args = ((List)map.get("args")).toArray();
        Map transactionMap = (Map)map.get("transactionBean");
        String beanName = (String)transactionMap.get("beanName");
        AbstractBizTran transactionBean = (AbstractBizTran) SpringContextsUtil.getBean(beanName, AbstractBizTran.class);
        BeanUtil.copyProperties(transactionMap, transactionBean);
        String[] parameterTypes = ((List<String>) map.get("parameterTypes")).toArray(new String[0]);
        String methodName = (String)map.get("methodName");
        List<Class> classArray = new ArrayList<Class>();

        for(int i = 0;i<args.length;i++) {
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
