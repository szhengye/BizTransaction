package com.bizmda.biztransaction.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SyncService {
    String confirmMethod() default "syncServiceConfirm";
    String commitMethod() default "syncServiceCommit";
    String rollbackMethod() default "syncServiceRollback";
}

