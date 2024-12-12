package com.sft.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname SftResponseFilter
 * @Description 过滤响应数据
 * @Date 2024/12/02 11:16
 * @Author by Sora33
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SftResponseFilter {
    Class<?> entity();
    String key() default "data";
    boolean preserveField() default true;
}
