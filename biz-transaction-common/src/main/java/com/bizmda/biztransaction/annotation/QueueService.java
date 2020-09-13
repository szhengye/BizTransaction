package com.bizmda.biztransaction.annotation;

import java.lang.annotation.*;

/**
 * 支持异步服务的自定义注解
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueueService {
    /**
     * 处理异步服务的排队队列
     * @return
     */
    String queue() default "queue.biztransaction.queueservice";
}