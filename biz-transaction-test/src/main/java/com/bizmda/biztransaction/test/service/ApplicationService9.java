package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.annotation.AsyncService;
import com.bizmda.biztransaction.annotation.QueueService;
import com.bizmda.biztransaction.annotation.SyncService;
import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
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
    /*
        处理标识：
        1:异步调用订单验证->异步调用微信支付
        2:异步调用订单验证->同步调用云闪付（超时后确认成功）->异步通知企业成功
        3:异步调用订单验证->同步调用云闪付（超时后确认失败）->异步通知企业失败
    */
    private int flag;

    @Autowired
    private TestOuterService testOuterService;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    /**
     * 交易调用入口
     *
     * @param inParams 交易输入参数
     * @return 交易返回参数
     * @throws TransactionException
     */
    @Override
    public Object doService(Object inParams) throws TransactionException {
        log.info("doService({})", inParams);
        String transactionKey = String.valueOf(Clock.systemDefaultZone().millis());
        log.info("1.调用订单验证");
        // 异步调用订单验证
        ((ApplicationService9) AopContext.currentProxy()).doAsyncOrderValid("company_order_valid", transactionKey);
        return "调用订单验证成功";
    }

    /**
     * 异步调用订单验证
     *
     * @param serviceId      为订单验证分配的服务唯一ID
     * @param transactionKey 在订单验证服务项下识别交易的唯一ID
     * @return 异步调用订单验证后同步返回的响应结果
     */
    @AsyncService(callbackMethod = "doAsyncOrderValidCallback")
    public Object doAsyncOrderValid(String serviceId, String transactionKey) {
        log.info("doAsyncOrderValid({},{})", serviceId, transactionKey);
        log.info("2.创建充值订单");
        log.info("3.用户录入验证要素");
        // 新起线程，等待5秒后在子线程中模拟订单验证完成后回调
        String result = (String) testOuterService.doServiceAsync("company_order_valid", transactionKey, "doAsyncOrderValid");
        log.info(result);
        return "doAsyncOrderValid()调用完成";
    }

    /**
     * 订单验证完成后的回调入口
     *
     * @param inParams 回调输入参数
     * @return 回调处理完成后给回调发起方的响应结果
     */
    public String doAsyncOrderValidCallback(Object inParams) {
        log.info("doAsyncOrderValidCallback({})", inParams);
        log.info("4.充值订单验证");
        log.info(("5.调用微信支付请求"));
        String transactionKey = String.valueOf(Clock.systemDefaultZone().millis());
        switch (this.flag) {
            case 1:
                // 异步调用微信支付请求
                String result = ((ApplicationService9) AopContext.currentProxy()).doAsyncWePayRequest("wepay", transactionKey);
                log.info(result);
                break;
            case 2:
            case 3:
                // 同步调用云闪付
                try {
                    ((ApplicationService9) AopContext.currentProxy()).doUnionPay(inParams);
                } catch (TransactionTimeOutException e) {
                    log.error("抛出超时异常!");
//                    e.printStackTrace();
                }
                break;
        }
        return "订单验证回调成功";
    }

    /**
     * 异步调用微信支付请求
     *
     * @param serviceId      为微信支付请求服务分配的服务唯一ID
     * @param transactionKey 微信支付请求服务项下识别交易的唯一ID
     * @return
     */
    @AsyncService(callbackMethod = "doAsyncWePayCallback")
    public String doAsyncWePayRequest(String serviceId, String transactionKey) {
        log.info("doAsyncWePayRequest({},{})", serviceId, transactionKey);
        log.info("6.创建支付订单");
        // 新起线程，等待5秒后在子线程中模拟微信支付完成后回调
        String result = (String) testOuterService.doServiceAsync(serviceId, transactionKey, "doAsyncWePayRequest");
        log.info(result);
        return "doAsyncWePayRequest()调用完成";
    }

    /**
     * 微信支付完成后的回调入口
     *
     * @param inParams 回调输入参数
     * @return 回调处理完成后给微信支付的响应结果
     */
    public String doAsyncWePayCallback(Object inParams) {
        log.info("doAsyncWePayCallback({})", inParams);
        log.info("7.支付订单完成");
        log.info("8.交易订单完成");
        ((ApplicationService9) AopContext.currentProxy()).sendMessage("微信支付交易成功!");
        return "微信支付回调成功";
    }

    /**
     * 同步调用云闪付，支持超时重试确认和失败回滚机制
     * @param inParams 输入参数
     * @return
     * @throws TransactionTimeOutException
     */
    @SyncService(confirmMethod = "confirmUnionPay", commitMethod = "commitUnionPay", rollbackMethod = "rollbackUnionPay")
    public boolean doUnionPay(Object inParams) throws TransactionTimeOutException {
        log.info("doUnionPay({})", inParams);
        log.info("6.模拟云闪付响应超时");
        testOuterService.doServiceOfTimeout();
        return true;
    }

    /**
     * 同步调用云闪付的重试确认方法
     * @return 重试确认成功/失败标识
     * @throws TransactionTimeOutException
     */
    public boolean confirmUnionPay() throws TransactionTimeOutException {
        log.info("confirmUnionPay()");
        if (this.flag == 2) {
            log.info("7.模拟同步调用云闪付（超时3次后确认成功）");
            return testOuterService.confirmService(true);
        }
        if (this.flag == 3) {
            log.info("7.模拟同步调用云闪付（超时3次后确认失败）");
            return testOuterService.confirmService(false);
        }
        return true;
    }

    /**
     * 同步调用云闪付的成功提交方法
     */
    public void commitUnionPay() {
        log.info("commitUnionPay()");
        log.info("8.支付订单完成");
        log.info("9.交易订单完成");
        ((ApplicationService9) AopContext.currentProxy()).sendMessage("云闪付交易成功!");
    }

    /**
     * 同步调用云闪付的失败回滚方法
     */
    public void rollbackUnionPay() {
        log.info("rollbackUnionPay()");
        log.info("8.支付订单回滚");
        log.info("9.交易订单回滚");
        ((ApplicationService9) AopContext.currentProxy()).sendMessage("云闪付交易失败!");
    }

    /**
     * 异步消息发送
     * @param message 要发送给企业的消息
     */
    @QueueService
    public void sendMessage(String message) {
        log.info("发送信息给企业:{}", message);
    }
}
