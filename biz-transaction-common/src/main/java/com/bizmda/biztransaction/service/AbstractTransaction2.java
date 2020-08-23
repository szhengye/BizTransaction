package com.bizmda.biztransaction.service;

import cn.hutool.core.bean.BeanUtil;
import com.bizmda.biztransaction.exception.Transaction2Exception;
import com.open.capacity.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractTransaction2 implements BeanNameAware {
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

    // 保存部署该Bean时指定的id属性
    private String beanName;
    public void setBeanName(String name)
    {
        this.beanName = name;
    }

    public String getBeanName() {
        return beanName;
    }

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

    public void saveState(String outerId, String transactionKey, long expiredTime) {
        String key = "biz:transaction2:" + outerId + ":" + transactionKey;
        this.redisUtil.set(key, this, expiredTime);
    }

    public abstract Object doServiceBeforeAsync(Object inParams) throws Transaction2Exception;
    public abstract Object doServiceAfterAsync(Object inParams) throws Transaction2Exception;
    public abstract void callbackTimeout() throws Transaction2Exception;
}
