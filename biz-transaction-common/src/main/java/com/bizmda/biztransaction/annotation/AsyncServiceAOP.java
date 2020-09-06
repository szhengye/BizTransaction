package com.bizmda.biztransaction.annotation;

import com.bizmda.biztransaction.service.AbstractTransaction;
import com.bizmda.biztransaction.service.RabbitmqSenderService;
import com.open.capacity.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Order(-1) // 保证该AOP在@Transactional之前执行
public class AsyncServiceAOP {
    @Autowired
    private RedisUtil redisUtil ;

//    public static ThreadLocal<Boolean> asyncServiceListener = new ThreadLocal<Boolean>();

    @Around(value = "@annotation(ds)")
    public Object doAsyncService(ProceedingJoinPoint joinPoint, AsyncService ds) throws Throwable {
        Object[] args = joinPoint.getArgs();// 参数值
        Map context = new HashMap();
        AbstractTransaction transactionBean = (AbstractTransaction)joinPoint.getTarget();
        context.put("transactionBean",transactionBean);
        context.put("callbackMethod",ds.callbackMethod());
        context.put("timeoutMethod",ds.timeoutMethod());
        String key = "biz:asyncservice:" + args[0] + ":" + args[1];
        this.redisUtil.set(key, context, ds.timeout());
        Object result = joinPoint.proceed(args);

        return result;
    }
}
