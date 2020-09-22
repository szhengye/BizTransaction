package com.bizmda.biztransaction.annotation;

import com.bizmda.biztransaction.exception.BizTranRespErrorException;
import com.bizmda.biztransaction.exception.BizTranTimeOutException;
import com.bizmda.biztransaction.service.AbstractBizTran;
import com.bizmda.biztransaction.service.RabbitmqSenderService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * 同步超时确认服务自定义注解的切面
 */
@Slf4j
@Aspect
@Order(-1)
public class SyncConfirmServiceAOP {
    @Autowired
    private RabbitmqSenderService rabbitmqSenderService;

    /**
     * 同步超时确认服务注解的环绕方法
     * @param joinPoint
     * @param ds
     * @return
     * @throws Throwable
     */
    @Around(value = "@annotation(ds)")
    public Object doSyncService(ProceedingJoinPoint joinPoint, SyncConfirmService ds) throws Throwable {
        AbstractBizTran transactionBean = (AbstractBizTran) joinPoint.getThis();
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
            Object result1 = joinPoint.proceed(args);
            Object result2 = commitMethod.invoke(transactionBean);
            return result1;

        } catch (BizTranTimeOutException e) {
            rabbitmqSenderService.sendSyncConfirmService(transactionBean, ds.confirmMethod(), ds.commitMethod(), ds.rollbackMethod());
            throw e;
        } catch (BizTranRespErrorException e) {
            rollbackMethod.invoke(transactionBean);
            throw e;
        }
    }
}
