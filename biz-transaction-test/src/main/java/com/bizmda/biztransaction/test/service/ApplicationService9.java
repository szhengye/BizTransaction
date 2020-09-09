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

    /**
     * 交易调用入口
     * @param inParams 交易输入参数
     * @return 交易返回参数
     * @throws TransactionException
     */
    @Override
    public Object doService(Object inParams) throws TransactionException {
        log.info("doService({})",inParams);
        String transactionKey = String.valueOf(Clock.systemDefaultZone().millis());
        log.info("1.调用订单验证");
        // 异步调用订单验证
        ((ApplicationService9) AopContext.currentProxy()).doAsyncOrderValid("company_order_valid",transactionKey);
        return "调用订单验证成功";
    }

    /**
     * 异步调用订单验证
     * @param serviceId 为订单验证分配的服务唯一ID
     * @param transactionKey 在订单验证服务项下识别交易的唯一ID
     * @return 异步调用订单验证后同步返回的响应结果
     */
    @AsyncService(callbackMethod = "doAsyncOrderValidCallback")
    public Object doAsyncOrderValid(String serviceId,String transactionKey) {
        log.info("doAsyncOrderValid({},{})", serviceId,transactionKey);
        log.info("2.创建充值订单");
        log.info("3.用户录入验证要素");
        // 新起线程，等待5秒后在子线程中模拟订单验证完成后回调
        String result = (String)testOuterService.doServiceAsync("company_order_valid",transactionKey,"doAsyncOrderValid");
        log.info(result);
        return "doAsyncOrderValid()调用完成";
    }

    /**
     * 订单验证完成后的回调入口
     * @param inParams 回调输入参数
     * @return 回调处理完成后给回调发起方的响应结果
     */
    public String doAsyncOrderValidCallback(Object inParams) {
        log.info("doAsyncOrderValidCallback({})", inParams);
        log.info("4.充值订单验证");
        log.info(("5.调用微信支付请求"));
        String transactionKey = String.valueOf(Clock.systemDefaultZone().millis());
        // 异步调用微信支付请求
        String result = ((ApplicationService9) AopContext.currentProxy()).doAsyncWePayRequest("wepay",transactionKey);
        log.info(result);
        return "订单验证回调成功";
    }

    /**
     * 异步调用微信支付请求
     * @param serviceId 为微信支付请求服务分配的服务唯一ID
     * @param transactionKey 微信支付请求服务项下识别交易的唯一ID
     * @return
     */
    @AsyncService(callbackMethod = "doAsyncWePayCallback")
    public String doAsyncWePayRequest(String serviceId,String transactionKey) {
        log.info("doAsyncWePayRequest({},{})", serviceId,transactionKey);
        log.info("6.创建支付订单");
        // 新起线程，等待5秒后在子线程中模拟微信支付完成后回调
        String result = (String)testOuterService.doServiceAsync(serviceId,transactionKey,"doAsyncWePayRequest");
        log.info(result);
        return "doAsyncWePayRequest()调用完成";
    }

    /**
     * 微信支付完成后的回调入口
     * @param inParams 回调输入参数
     * @return 回调处理完成后给微信支付的响应结果
     */
    public String doAsyncWePayCallback(Object inParams) {
        log.info("doAsyncWePayCallback({})", inParams);
        log.info("7.支付订单完成");
        log.info("8.交易订单完成");
        return "微信支付回调成功";
    }
}
