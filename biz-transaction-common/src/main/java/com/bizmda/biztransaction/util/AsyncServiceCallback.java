package com.bizmda.biztransaction.util;

import cn.hutool.core.bean.BeanUtil;
import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.service.AbstractBizTran;
import com.open.capacity.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 异步回调工具类
 */
@Slf4j
@Service
public class AsyncServiceCallback {
    @Autowired
    private RedisUtil redisUtil ;

    /**
     * 外部服务异步回调后，应由开发者主动调用的方法，以触发回调后的业务逻辑
     * @param outerId 服务id
     * @param transactionKey 交易唯一主键
     * @param inParams 回调输入参数
     * @return 处理返回结果
     * @throws TransactionException
     */
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
        AbstractBizTran transaction2 = (AbstractBizTran) SpringContextsUtil.getBean(beanName, AbstractBizTran.class);
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
