package com.bizmda.biztransaction.service;

import cn.hutool.core.bean.BeanUtil;
import com.bizmda.biztransaction.exception.BizTranException;
import com.open.capacity.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;

/**
 * 异步回调服务的抽象处理类
 */
@Slf4j
public abstract class AbstractBizTran2 extends AbstractBizTran {
    @Autowired
    private RedisUtil redisUtil ;

    /**
     * 服务调用的统一入口
     * @param inParams 服务调用参数
     * @return 服务返回结果
     * @throws BizTranException
     */
    @Override
    public Object doService(Object inParams) throws BizTranException {
        return this.doServiceBeforeAsync(inParams);
    }

    /**
     * 在调用外部异步服务前，主动保存交易状态，一般在doServiceBeforeAsync()方法束时应执行，强烈建议在调用用外部通讯前执行。
     * @param outerId 服务id
     * @param transactionKey 交易唯一主键
     * @param expiredTime 交易超时时间
     */
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

    /**
     * 实现调用外部异步服务之前的业务逻辑
     * @param inParams 调用输入参数
     * @return 返回结果
     * @throws BizTranException
     */
    public abstract Object doServiceBeforeAsync(Object inParams) throws BizTranException;

    /**
     * 实现外部异步服务回调后的业务逻辑
     * @param inParams 调用输入参数
     * @return 返回结果
     * @throws BizTranException
     */
    public abstract Object doServiceAfterAsync(Object inParams) throws BizTranException;

    /**
     * 外部异步服务在约定的时间内没有发生回调操作，触发的业务逻辑
     * @throws BizTranException
     */
    public abstract void callbackTimeout() throws BizTranException;
}
