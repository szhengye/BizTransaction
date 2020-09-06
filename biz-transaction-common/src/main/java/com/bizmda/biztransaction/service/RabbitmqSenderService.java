package com.bizmda.biztransaction.service;

import cn.hutool.core.bean.BeanUtil;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RabbitMQ
 * 发送ttl消息
 **/
@Slf4j
@Service
public class RabbitmqSenderService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public static String[] expirationArray = {"0","2000","4000","8000","16000","32000","64000"};

    public void sendQueueService(String queueName,AbstractTransaction transactionBean, String methodName, List<String> parameterTypes,Object[] args) {
        Map context = new HashMap();
        context.put("transactionBean",transactionBean);
        context.put("methodName",methodName);
        context.put("parameterTypes",parameterTypes);
        context.put("args",args);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.convertAndSend(queueName,context);
    }

    public void sendSyncService(AbstractTransaction transactionBean, String confirmMethod, String commitMethod, String rollbackMethod) {
        Map context = new HashMap();
        Map transactionMap = new HashMap();
        BeanUtil.copyProperties(transactionBean,transactionMap);
        context.put("transactionBean",transactionMap);
        context.put("confirmMethod",confirmMethod);
        context.put("commitMethod",commitMethod);
        context.put("rollbackMethod",rollbackMethod);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setExchange("dead.prod.exchange");
        rabbitTemplate.setRoutingKey("dead.prod.routing.key");
        rabbitTemplate.convertAndSend(context, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                MessageProperties mp = message.getMessageProperties();
                mp.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
//                mp.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME, Map.class);

                //动态设置TTL
                log.info("RabbitmqSenderService.expirationArray[transactionBean.getConfirmTimes()]:{}",RabbitmqSenderService.expirationArray[transactionBean.getConfirmTimes()]);
                mp.setExpiration(RabbitmqSenderService.expirationArray[transactionBean.getConfirmTimes()]);
                return message;
            }
        });
    }

//    public void sendOuterServiceConfirmMsg(AbstractTransaction1 transactionBean) throws TransactionMaxConfirmFailException {
//
//        if (transactionBean.getConfirmTimes() >= RabbitSenderService.expirationArray.length - 2) {
//            throw new TransactionMaxConfirmFailException();
//        }
////        Map map = Maps.newHashMap();
////        map.put("type",String.valueOf(type));
////        map.put("beanName", beanName);
////        map.put("no", String.valueOf(no));
////        map.put("msg", msg);
////        map.put("transactionBean", transactionBean);
//        log.info("***transactionBean:{}",transactionBean.getClass().getName());
//        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
//        rabbitTemplate.setExchange("dead.prod.exchange");
//        rabbitTemplate.setRoutingKey("dead.prod.routing.key");
//        rabbitTemplate.convertAndSend(transactionBean, new MessagePostProcessor() {
//            @Override
//            public Message postProcessMessage(Message message) throws AmqpException {
//                MessageProperties mp = message.getMessageProperties();
//                mp.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
////                mp.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME, Map.class);
//
//                //动态设置TTL
//                mp.setExpiration(RabbitSenderService.expirationArray[transactionBean.getConfirmTimes()]);
//                return message;
//            }
//        });
//        log.info("sendTTLExpireMsg({})",transactionBean);
//        log.info("Message expiration：" + RabbitSenderService.expirationArray[transactionBean.getConfirmTimes()]);
//
//    }
    /**
     * 发送信息入死信队列
     */
    public void sendTTLExpireMsg(int type, String beanName, int no, Object msg, Object transactionBean) throws TransactionMaxConfirmFailException {

        if (no >= RabbitmqSenderService.expirationArray.length - 2) {
            throw new TransactionMaxConfirmFailException();
        }
        Map map = Maps.newHashMap();
        map.put("type",String.valueOf(type));
        map.put("beanName", beanName);
        map.put("no", String.valueOf(no));
        map.put("msg", msg);
        map.put("transactionBean", transactionBean);
        log.info("***transactionBean:{}",transactionBean.getClass().getName());
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
                mp.setExpiration(RabbitmqSenderService.expirationArray[no]);
                return message;
            }
        });
        log.info("sendTTLExpireMsg({}, {}, {}, {}, {})",type, beanName, no, msg, transactionBean);
        log.info("Message expiration：" + RabbitmqSenderService.expirationArray[no]);

    }

}




























