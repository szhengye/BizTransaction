package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.annotation.AsyncService;
import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.service.AbstractTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Slf4j
@Service
@Scope("prototype")
public class ApplicationService9 extends AbstractTransaction {

    @Autowired
    private TestOuterService testOuterService ;

    @Override
    public Object doService(Object inParams) throws TransactionException {
        String transactionKey = String.valueOf(Clock.systemDefaultZone().millis());
        log.info("1.调用订单验证");
        ((ApplicationService9) AopContext.currentProxy()).doAsyncOrderValid("company_order_valid",transactionKey);
        return "调用订单验证成功";
    }

    @AsyncService(callbackMethod = "doAsyncOrderValidCallback")
    public Object doAsyncOrderValid(String serviceId,String transactionKey) {
        log.info("doAsyncOrderValid({},{})", serviceId,transactionKey);
        log.info("2.创建充值订单");
        log.info("3.用户录入验证要素");
        String result = (String)testOuterService.doServiceAsync("company_order_valid",transactionKey,"doAsyncOrderValid");
        log.info(result);
        return "doAsyncOrderValid()调用完成";
    }

    public String doAsyncOrderValidCallback(Object inParams) {
        log.info("doAsyncOrderValidCallback({})", inParams);
        log.info("4.充值订单验证");
        log.info(("5.调用微信支付请求"));
        String transactionKey = String.valueOf(Clock.systemDefaultZone().millis());
        String result = ((ApplicationService9) AopContext.currentProxy()).doAsyncWePayRequest("wepay",transactionKey);
        log.info(result);
        return "订单验证回调成功";
    }

    @AsyncService(callbackMethod = "doAsyncWePayCallback")
    public String doAsyncWePayRequest(String serviceId,String transactionKey) {
        log.info("doAsyncWePayRequest({},{})", serviceId,transactionKey);
        log.info("6.创建支付订单");
        String result = (String)testOuterService.doServiceAsync(serviceId,transactionKey,"doAsyncWePayRequest");
        log.info(result);
        return "doAsyncWePayRequest()调用完成";
    }

    public String doAsyncWePayCallback(Object inParams) {
        log.info("doAsyncWePayCallback({})", inParams);
        log.info("8.支付订单完成");
        log.info("9.交易订单完成");
        return "微信支付回调成功";
    }
}
