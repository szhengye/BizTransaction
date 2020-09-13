package com.bizmda.biztransaction.annotation;

import com.bizmda.biztransaction.service.AbstractBizTran;
import com.bizmda.biztransaction.service.RabbitmqSenderService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * 异步服务注释的切面
 */
@Slf4j
@Aspect
@Order(-1)
public class QueueServiceAOP {
    public static ThreadLocal<Boolean> queueServiceListener = new ThreadLocal<Boolean>();
    @Autowired
    private RabbitmqSenderService rabbitmqSenderService;

    /**
     * 异步服务的环绕方法
     * @param joinPoint
     * @param ds
     * @return
     * @throws Throwable
     */
    @Around("@annotation(ds)")
    public Object doQueueService(ProceedingJoinPoint joinPoint, QueueService ds) throws Throwable {
        if (QueueServiceAOP.queueServiceListener.get() != null) {
            QueueServiceAOP.queueServiceListener.remove();
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

        Object[] args = joinPoint.getArgs();
        AbstractBizTran tranBean = (AbstractBizTran)joinPoint.getThis();
        rabbitmqSenderService.sendQueueService(ds.queue(),tranBean,methodName,parameterTypes,args);
        return null;
    }
}
