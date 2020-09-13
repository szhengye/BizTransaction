package com.bizmda.biztransaction.util;

import cn.hutool.core.bean.BeanUtil;
import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.service.AbstractTransaction;
import com.bizmda.biztransaction.util.SpringContextsUtil;
import com.open.capacity.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
@Service
public class AsyncServiceCallback {
    @Autowired
    private RedisUtil redisUtil ;

    // 外部服务异步回调后，应由开发者主动调用的方法，以触发回调后的业务逻辑
    public Object callback(String outerId, String transactionKey, Object inParams) throws TransactionException {
        log.info("callback({},{},{})",outerId,transactionKey,inParams);
        String key = "biz:asyncservice:" + outerId + ":" + transactionKey;
        String preKey = "biz:pre_asyncservice:" + outerId + ":" + transactionKey;
        Map context = (Map)this.redisUtil.get(key);
        if (context == null) {
            throw new TransactionException(TransactionException.NO_MATCH_TRANSACTION_EXCEPTION_CODE);
        }
        this.redisUtil.del(key);
        this.redisUtil.del(preKey);
//        log.info("callback context:{}",context);
        Map transactionMap = (Map)context.get("transactionBean");
        String callbackMethodName = (String)context.get("callbackMethod");
//        String timeoutMethod = (String)context.get("timeoutMethod");


        String beanName = (String)transactionMap.get("beanName");
        AbstractTransaction transaction2 = (AbstractTransaction) SpringContextsUtil.getBean(beanName, AbstractTransaction.class);
        BeanUtil.copyProperties(transactionMap, transaction2);

//        log.info("callback transaction2:{},{}",callbackMethodName,transaction2);
        Method callbackMethod = null;
        try {
            callbackMethod = transaction2.getClass().getMethod(callbackMethodName,Object.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }

        try {
            return callbackMethod.invoke(transaction2,inParams);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
