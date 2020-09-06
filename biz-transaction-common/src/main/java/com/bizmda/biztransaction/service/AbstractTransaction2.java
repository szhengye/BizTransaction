package com.bizmda.biztransaction.service;

import cn.hutool.core.bean.BeanUtil;
import com.bizmda.biztransaction.exception.TransactionException;
import com.open.capacity.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractTransaction2 extends AbstractTransaction {
    @Autowired
    private RedisUtil redisUtil ;

    public Object doService(Object inParams) throws TransactionException {
        return this.doServiceBeforeAsync(inParams);
    }

    public void abortTransaction(Throwable e) {
        log.info("abortTransaction:{}",e.getMessage());
    }

    // 在调用外部异步服务前，主动保存交易状态，一般在doServiceBeforeAsync()方法束时应执行，强烈建议在调用用外部通讯前执行。
    public void saveState(String outerId, String transactionKey, long expiredTime) {
        log.info("saveState({},{},{})",outerId,transactionKey,expiredTime);
        String key = "biz:asyncservice:" + outerId + ":" + transactionKey;
        Map context = new HashMap();
        Map transactionMap = new HashMap();
        BeanUtil.copyProperties(this,transactionMap);
        context.put("transactionBean",transactionMap);
        context.put("callbackMethod","doServiceAfterAsync");
        context.put("timeoutMethod","callbackTimeout");
        this.redisUtil.set(key, context, expiredTime);
    }

    // 实现调用外部异步服务之前的业务逻辑
    public abstract Object doServiceBeforeAsync(Object inParams) throws TransactionException;
    // 实现外部异步服务回调后的业务逻辑
    public abstract Object doServiceAfterAsync(Object inParams) throws TransactionException;
    // 外部异步服务在约定的时间内没有发生回调操作，触发的业务逻辑
    public abstract void callbackTimeout() throws TransactionException;
}
