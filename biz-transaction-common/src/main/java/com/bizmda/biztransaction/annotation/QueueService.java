package com.bizmda.biztransaction.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueueService {
    String queue() default "queue.biztransaction.queueservice";
}