package com.bizmda.biztransaction.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AsyncService {
    String callbackMethod() default "asyncServiceCallback";
    String timeoutMethod() default "asyncServiceTimeout";
    int timeout() default 60;
}