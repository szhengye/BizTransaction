package com.bizmda.biztransaction.service;

import com.bizmda.biztransaction.exception.TransactionMaxConfirmFailException;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * RabbitMQ
 * 发送ttl消息
 **/
@Slf4j
@Service
public class RabbitSenderService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static String[] expirationArray = {"0","2000","4000","8000","16000","32000","64000"};

    /**
     * 发送信息入死信队列
     */
    public void sendTTLExpireMsg(int type, String beanName, int no, Object msg, Map context) throws TransactionMaxConfirmFailException {

        if (no >= RabbitSenderService.expirationArray.length - 2) {
            throw new TransactionMaxConfirmFailException();
        }
        Map map = Maps.newHashMap();
        map.put("type",String.valueOf(type));
        map.put("beanName", beanName);
        map.put("no", String.valueOf(no));
        map.put("msg", msg);
        map.put("context", context);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setExchange("dead.prod.exchange");
        rabbitTemplate.setRoutingKey("dead.prod.routing.key");
        rabbitTemplate.convertAndSend(map, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                MessageProperties mp = message.getMessageProperties();
                mp.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                mp.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME, Map.class);

                //动态设置TTL
                mp.setExpiration(RabbitSenderService.expirationArray[no]);
                return message;
            }
        });
        log.info("sendTTLExpireMsg({}, {}, {}, {}, {})",type, beanName, no, msg, context);
        log.info("Message expiration：" + RabbitSenderService.expirationArray[no]);

    }

}




























