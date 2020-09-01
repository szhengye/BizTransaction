package com.bizmda.biztransaction.annotation;

import com.bizmda.biztransaction.service.AbstractTransaction;
import com.bizmda.biztransaction.service.RabbitSenderService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Aspect
@Order(-1) // 保证该AOP在@Transactional之前执行
public class SyncServiceAOP {
    public static ThreadLocal<Boolean> syncServiceListener = new ThreadLocal<Boolean>();
    @Autowired
    private RabbitSenderService rabbitSenderService ;

    @Around("@annotation(ds)")
    public Object doQueueService(ProceedingJoinPoint joinPoint, QueueService ds) throws Throwable {
        if (SyncServiceAOP.syncServiceListener.get() != null) {
            Object result = joinPoint.proceed();
            return result;
        }
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String methodName = methodSignature.getName();
        Class[] classArray = methodSignature.getParameterTypes();
        List<String> parameterTypes = new ArrayList<String>();
        for(Class cls:classArray) {
            parameterTypes.add(cls.getName());
        }

        Object[] args = joinPoint.getArgs();// 参数值
        AbstractTransaction tranBean = (AbstractTransaction)joinPoint.getTarget();
        rabbitSenderService.sendQueueService(ds.queue(),tranBean,methodName,parameterTypes,args);
        return null;
    }
}
