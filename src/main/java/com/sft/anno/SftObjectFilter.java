package com.sft.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname SftObjectFilter
 * @Description 过滤对象类型
 * @Date 2024/11/14 10:50
 * @Author by Sora33
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SftObjectFilter {
    Class<?> entity();
    boolean preserveField() default true;
}
