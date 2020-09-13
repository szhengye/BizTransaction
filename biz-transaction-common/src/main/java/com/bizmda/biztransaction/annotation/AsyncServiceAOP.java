package com.bizmda.biztransaction.annotation;

import cn.hutool.core.bean.BeanUtil;
import com.bizmda.biztransaction.service.AbstractBizTran;
import com.open.capacity.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.util.HashMap;
import java.util.Map;

/**
 * 异步服务回调切面
 */
@Slf4j
@Aspect
@Order(-1)
public class AsyncServiceAOP {
    /**
     * 设置实际存储数据的Redis主键和过期Redis主键的统一间隔时间
     */
    private final int secondsAfterExpired = 60;
    @Autowired
    private RedisUtil redisUtil ;

    /**
     * 异步服务回调自定义注解的环绕方法
     * @param joinPoint
     * @param ds
     * @return
     * @throws Throwable
     */
    @Around(value = "@annotation(ds)")
    public Object doAsyncService(ProceedingJoinPoint joinPoint, AsyncService ds) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Map context = new HashMap();
        AbstractBizTran transactionBean = (AbstractBizTran)joinPoint.getThis();
        Map transactionMap = new HashMap();
        BeanUtil.copyProperties(transactionBean,transactionMap);
        context.put("transactionBean",transactionMap);
        context.put("callbackMethod",ds.callbackMethod());
        context.put("timeoutMethod",ds.timeoutMethod());
        String preKey = "biz:pre_asyncservice:" + args[0] + ":" + args[1];
        String key = "biz:asyncservice:" + args[0] + ":" + args[1];
        log.info("time:{},{}",ds.timeout(),ds.timeout()+this.secondsAfterExpired);
        this.redisUtil.set(preKey, "", ds.timeout());
        this.redisUtil.set(key, context, ds.timeout() + this.secondsAfterExpired);
        Object result = joinPoint.proceed(args);

        return result;
    }
}
