package com.bizmda.biztransaction.service;

import com.bizmda.biztransaction.config.RabbitmqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * RabbitMQ接收服务
 */
@Slf4j
@Service
public class RabbitmqReceiverService {
    @Autowired
    private  BizTranService bizTranService;

    /**
     * 同步确认服务重试队列的侦听消费处理
     * @param map 传入的重试消息
     */
    @RabbitListener(queues = RabbitmqConfig.SYNC_CONFIRM_SERVICE_QUEUE, containerFactory = "multiListenerContainer")
    public void syncConfirmServiceListener(Map map) {
        bizTranService.syncConfirmServiceReceive(map);
    }

    /**
     * 异步通知服务队列的侦听消费处理
     * @param map 传入的异步通知服务消息
     */
    @RabbitListener(queues = RabbitmqConfig.QUEUE_SERVICE_QUEUE, containerFactory = "multiListenerContainer")
    public void queueServiceListener(Map map) {
        bizTranService.queueServiceService(map);
    }
}
