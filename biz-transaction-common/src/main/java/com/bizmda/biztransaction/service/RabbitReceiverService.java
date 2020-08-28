package com.bizmda.biztransaction.service;

import cn.hutool.core.bean.BeanUtil;
import com.bizmda.biztransaction.exception.TransactionMaxConfirmFailException;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * RabbitMQ接收消息服务
 **/
@Slf4j
@Service
public class RabbitReceiverService {
    @Autowired
    private RabbitSenderService rabbitSenderService;

    /**
     * ttl消息
     *
     * @param map
     */
    @RabbitListener(queues = {"dead.real.queue"}, containerFactory = "multiListenerContainer")
    public void consumeExpirMsg(Map map) {
        log.info("***receive:{}", map);
        String beanName = (String) map.get("beanName");
        AbstractTransaction1 transaction1 = (AbstractTransaction1) SpringContextsUtil.getBean(beanName, AbstractTransaction1.class);
        BeanUtil.copyProperties(map, transaction1);
        log.info("***receive:transactionBean:{},{}", transaction1.getClass().getName(), transaction1);
        try {
            if (transaction1.confirmOuterService()) {
                transaction1.doInnerService2();
            } else {
                transaction1.cancelInnerService1();
            }
        } catch (TransactionTimeOutException e) {
            transaction1.setConfirmTimes(transaction1.getConfirmTimes() + 1);
            try {
                rabbitSenderService.sendOuterServiceConfirmMsg(transaction1);
            } catch (TransactionMaxConfirmFailException transactionMaxConfirmFailException) {
                transaction1.abortTransaction(transactionMaxConfirmFailException);
            }
        }

    }
}












