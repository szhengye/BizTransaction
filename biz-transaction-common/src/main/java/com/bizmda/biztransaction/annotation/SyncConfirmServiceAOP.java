package com.bizmda.biztransaction.annotation;

import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import com.bizmda.biztransaction.service.AbstractTransaction;
import com.bizmda.biztransaction.service.RabbitmqSenderService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Order(-1) // 保证该AOP在@Transactional之前执行
public class SyncConfirmServiceAOP {
    //    public static ThreadLocal<Boolean> syncServiceListener = new ThreadLocal<Boolean>();
    @Autowired
    private RabbitmqSenderService rabbitmqSenderService;

    @Around(value = "@annotation(ds)")
    public Object doSyncService(ProceedingJoinPoint joinPoint, SyncConfirmService ds) throws Throwable {
        AbstractTransaction transactionBean = (AbstractTransaction) joinPoint.getThis();
        transactionBean.setConfirmTimes(0);
        Method confirmMethod = null;
        Method commitMethod = null;
        Method rollbackMethod = null;
        try {
            confirmMethod = transactionBean.getClass().getMethod(ds.confirmMethod());
            commitMethod = transactionBean.getClass().getMethod(ds.commitMethod());
            rollbackMethod = transactionBean.getClass().getMethod(ds.rollbackMethod());
        } catch (NoSuchMethodException e) {
            throw e;
        }
        Object[] args = joinPoint.getArgs();// 参数值
        try {
            Boolean result = (Boolean) joinPoint.proceed(args);
            if (result) {
                commitMethod.invoke(transactionBean);
            } else {
                rollbackMethod.invoke(transactionBean);
            }
            return result;
//        } catch (InvocationTargetException e) {
////            log.info("szy:1");
//            if (e.getTargetException().getClass().equals(TransactionTimeOutException.class)) {
////                log.info("szy:2");
//                transactionBean.setConfirmTimes(transactionBean.getConfirmTimes() + 1);
//                rabbitmqSenderService.sendSyncService(transactionBean, ds.confirmMethod(), ds.commitMethod(), ds.rollbackMethod());
//            }
//            throw e;
        } catch (TransactionTimeOutException e) {
//            log.info("szy:2");
//            transactionBean.setConfirmTimes(transactionBean.getConfirmTimes() + 1);
            rabbitmqSenderService.sendSyncService(transactionBean, ds.confirmMethod(), ds.commitMethod(), ds.rollbackMethod());
            throw e;
        }
    }
}
