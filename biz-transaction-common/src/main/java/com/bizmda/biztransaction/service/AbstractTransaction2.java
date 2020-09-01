package com.bizmda.biztransaction.service;

import cn.hutool.core.bean.BeanUtil;
import com.bizmda.biztransaction.exception.Transaction1Exception;
import com.bizmda.biztransaction.exception.Transaction2Exception;
import com.open.capacity.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractTransaction2 extends AbstractTransaction {
    @Autowired
    private RedisUtil redisUtil ;

    private static RedisUtil staticRedisUtil;

//    @Autowired
//    public AbstractTransaction2(RedisUtil staticRedisUtil) {
//        AbstractTransaction2.staticRedisUtil = staticRedisUtil;
//    }

    @Autowired
    public void setStaticRedisUtil(RedisUtil staticRedisUtil){
        AbstractTransaction2.staticRedisUtil = staticRedisUtil;
    }

    public Object doService(Object inParams) throws Transaction2Exception {
        return this.doServiceBeforeAsync(inParams);
    }

    // 外部服务异步回调后，应由开发者主动调用的方法，以触发回调后的业务逻辑
    public static void callback(String outerId, String transactionKey, Object inParams) throws Transaction2Exception {
        String key = "biz:transaction2:" + outerId + ":" + transactionKey;
        AbstractTransaction2 transaction2Redis = (AbstractTransaction2)AbstractTransaction2.staticRedisUtil.get(key);
        if (transaction2Redis == null) {
            throw new Transaction2Exception(Transaction2Exception.NO_MATCH_TRANSACTION_EXCEPTION_CODE);
        }
        String beanName = transaction2Redis.getBeanName();
        AbstractTransaction2 transaction2 = (AbstractTransaction2) SpringContextsUtil.getBean(beanName, AbstractTransaction2.class);
        BeanUtil.copyProperties(transaction2Redis, transaction2);
        transaction2.doServiceAfterAsync(inParams);
    }

    public void abortTransaction(Throwable e) {
        log.info("abortTransaction:{}",e.getMessage());
    }

    // 在调用外部异步服务前，主动保存交易状态，一般在doServiceBeforeAsync()方法束时应执行，强烈建议在调用用外部通讯前执行。
    public void saveState(String outerId, String transactionKey, long expiredTime) {
        String key = "biz:transaction2:" + outerId + ":" + transactionKey;
        this.redisUtil.set(key, this, expiredTime);
    }

    // 实现调用外部异步服务之前的业务逻辑
    public abstract Object doServiceBeforeAsync(Object inParams) throws Transaction2Exception;
    // 实现外部异步服务回调后的业务逻辑
    public abstract Object doServiceAfterAsync(Object inParams) throws Transaction2Exception;
    // 外部异步服务在约定的时间内没有发生回调操作，触发的业务逻辑
    public abstract void callbackTimeout() throws Transaction2Exception;
}
