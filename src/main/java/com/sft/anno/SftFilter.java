package com.sft.anno;

import java.lang.annotation.*;

/**
 * @Classname SftFilter
 * @Description 过滤字段
 * @Date 2024/11/14 10:51
 * @Author by Sora33
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SftFilter {
    String value() default "null";
}
