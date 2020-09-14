package com.bizmda.biztransaction.service;

import cn.hutool.core.bean.BeanUtil;
import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.util.SpringContextsUtil;
import com.open.capacity.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 对Redis过期Key的监听
 */
@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    @Autowired
    private  BizTranService bizTranService;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * key过期监听事件
     * @param message
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("expiredKey:{}",expiredKey);
        if (expiredKey.startsWith("biz:asyncservice:")) {
            this.doKeyExpire(expiredKey);
        }
        else if (expiredKey.startsWith("biz:pre_asyncservice:")) {
            try {
                this.doPreKeyExpire(expiredKey);
            } catch (TransactionException e) {
                e.printStackTrace();
            }
        }
        else {
            log.warn("Redis键超时:{}",expiredKey);
        }
    }

    /**
     * 副key直接过期，应做异常处理和记录
     * @param key
     */
    private void doKeyExpire(String key) {
        String[] a = key.split(":");
        log.error("调用交易[{},{}]出错：没有触发Redis键超时，导致无法执行TimeoutMethod方法！");
    }

    /**
     * 主key过期，取出副key键值进行处理
     * @param preKey
     * @throws TransactionException
     */
    private void doPreKeyExpire(String preKey) throws TransactionException {
        bizTranService.asyncServiceTimeout(preKey);
    }
}