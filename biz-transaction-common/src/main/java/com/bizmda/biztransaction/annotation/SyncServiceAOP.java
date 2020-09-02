package com.bizmda.biztransaction.annotation;

import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import com.bizmda.biztransaction.service.AbstractTransaction;
import com.bizmda.biztransaction.service.RabbitmqSenderService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Aspect
@Order(-1) // 保证该AOP在@Transactional之前执行
public class SyncServiceAOP {
    public static ThreadLocal<Boolean> syncServiceListener = new ThreadLocal<Boolean>();
    @Autowired
    private RabbitmqSenderService rabbitmqSenderService;

    @Around("@annotation(ds)")
    public Object doSyncService(ProceedingJoinPoint joinPoint, SyncService ds) throws Throwable {
        log.info("sssss-1");
        if (SyncServiceAOP.syncServiceListener.get() != null) {
            Object[] args = joinPoint.getArgs();// 参数值
            log.info("sssss-1");
            try {
                Object result = joinPoint.proceed(args);
                return result;
            } catch (Throwable e) {
                log.info("ssssss0");
                e.printStackTrace();
            }
//            } catch (InvocationTargetException e) {
//                log.info("ssssss1");
//                if (e.getTargetException().getClass().equals(TransactionTimeOutException.class)) {
//                    log.info("ssssss2");
//                    AbstractTransaction tranBean = (AbstractTransaction) joinPoint.getTarget();
//                    rabbitmqSenderService.sendSyncService(tranBean, ds.confirmMethod(), ds.commitMethod(), ds.rollbackMethod());
//                }
//                throw e;
//            }
        }

        AbstractTransaction tranBean = (AbstractTransaction) joinPoint.getTarget();
        rabbitmqSenderService.sendSyncService(tranBean, ds.confirmMethod(), ds.commitMethod(), ds.rollbackMethod());
        return null;
    }
}
