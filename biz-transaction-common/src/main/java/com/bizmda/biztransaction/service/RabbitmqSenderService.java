package com.bizmda.biztransaction.service;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RabbitMQ发送服务
 **/
@Slf4j
@Service
public class RabbitmqSenderService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 同步确认交易的重复重试时间间隔和次数
     */
    public static String[] expirationArray = {"0","2000","4000","8000","16000","32000","64000"};

    /**
     * 通过RabbitMQ发送异步处理服务消息
     * @param queueName 发送所用的消息队列名
     * @param transactionBean 发送的交易实体类
     * @param methodName 发送的异步执行方法名称
     * @param parameterTypes 异步执行方法的各参数类型
     * @param args 异步执行方法的各参数值
     */
    public void sendQueueService(String queueName, AbstractBizTran transactionBean, String methodName, List<String> parameterTypes, Object[] args) {
        Map context = new HashMap();
        Map transactionMap = new HashMap();
        BeanUtil.copyProperties(transactionBean,transactionMap);
        transactionMap.put("tranContext",transactionBean.getTranContext());
        transactionMap.put("confirmTimes",transactionBean.getConfirmTimes());
        context.put("transactionBean",transactionMap);
        context.put("methodName",methodName);
        context.put("parameterTypes",parameterTypes);
        context.put("args",args);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setExchange(null);
        rabbitTemplate.convertAndSend(queueName,context);
    }

    /**
     * 通过RabbitMQ发送同步确认重试消息
     * @param transactionBean 交易实体类
     * @param confirmMethod 执行确认重试的方法
     * @param commitMethod 重试成功后的提交方法
     * @param rollbackMethod 重试失败后的回滚方法
     */
    public void sendSyncConfirmService(AbstractBizTran transactionBean, String confirmMethod, String commitMethod, String rollbackMethod) {
        log.info("sendSyncService({},{},{},{})",transactionBean,confirmMethod,commitMethod,rollbackMethod);
        Map context = new HashMap();
        Map transactionMap = new HashMap();
        BeanUtil.copyProperties(transactionBean,transactionMap);
        transactionMap.put("tranContext",transactionBean.getTranContext());
        transactionMap.put("confirmTimes",transactionBean.getConfirmTimes());
        transactionMap.put("confirmTimes",transactionBean.getConfirmTimes());
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
//                log.info("RabbitmqSenderService.expirationArray[transactionBean.getConfirmTimes()]:{}",RabbitmqSenderService.expirationArray[transactionBean.getConfirmTimes()]);
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
//    /**
//     * 发送信息入死信队列
//     */
//    public void sendTTLExpireMsg(int type, String beanName, int no, Object msg, Object transactionBean) throws TransactionMaxConfirmFailException {
//
//        if (no >= RabbitmqSenderService.expirationArray.length - 1) {
//            throw new TransactionMaxConfirmFailException();
//        }
//        Map map = Maps.newHashMap();
//        map.put("type",String.valueOf(type));
//        map.put("beanName", beanName);
//        map.put("no", String.valueOf(no));
//        map.put("msg", msg);
//        map.put("transactionBean", transactionBean);
//        log.info("***transactionBean:{}",transactionBean.getClass().getName());
//        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
//        rabbitTemplate.setExchange("dead.prod.exchange");
//        rabbitTemplate.setRoutingKey("dead.prod.routing.key");
//        rabbitTemplate.convertAndSend(map, new MessagePostProcessor() {
//            @Override
//            public Message postProcessMessage(Message message) throws AmqpException {
//                MessageProperties mp = message.getMessageProperties();
//                mp.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
//                mp.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME, Map.class);
//
//                //动态设置TTL
//                mp.setExpiration(RabbitmqSenderService.expirationArray[no]);
//                return message;
//            }
//        });
//        log.info("sendTTLExpireMsg({}, {}, {}, {}, {})",type, beanName, no, msg, transactionBean);
//        log.info("Message expiration：" + RabbitmqSenderService.expirationArray[no]);
//
//    }

}




























