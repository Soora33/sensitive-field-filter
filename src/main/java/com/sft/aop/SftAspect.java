package com.sft.aop;

import com.sft.anno.SftObjectFilter;
import com.sft.anno.SftResponseFilter;
import com.sft.util.SftUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
@EnableAspectJAutoProxy
public class SftAspect {

    private static final Logger logger = LoggerFactory.getLogger(SftAspect.class);

    // 过滤Response
    @Around("@annotation(com.sft.anno.SftResponseFilter)")
    public Object SftResponseFilter(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            SftResponseFilter annotation = AnnotationUtils.findAnnotation(signature.getMethod(), SftResponseFilter.class);
            Class<?> entity = annotation.entity();
            String key = annotation.key();
            boolean preserveField = annotation.preserveField();

            Object result = joinPoint.proceed();

            if (result != null && !entity.isAssignableFrom(result.getClass())) {
                logger.error("配置类型与实际返回类型不匹配: 配置类型 [{}], 实际返回类型 [{}]", entity.getName(), result.getClass().getName());
            }

            if (result instanceof Map) {
                Map<Object, Object> resultMap = (Map<Object, Object>) result;
                Object value = resultMap.get(key);
                if (value != null) {
                    LinkedHashMap<Object, Object> linkedHashMap = SftUtil.filterFieldsToMap(value, preserveField);
                    resultMap.put(key, linkedHashMap);
                }
                return resultMap;
            }

            // 如果不是Map类型，保持原有的对象字段处理逻辑
            if (result != null && entity.isAssignableFrom(result.getClass())) {
                Field field = getField(result.getClass(), key);
                if (field != null) {
                    field.setAccessible(true);
                    Object object = field.get(result);
                    if (!preserveField) {
                        field.set(result, SftUtil.filterFieldsToMap(object, preserveField));
                    }
                    SftUtil.filterFields(object);
                    return result;
                }
            }
            return result;
        } catch (Throwable e) {
            logger.error("处理响应过滤时发生错误", e);
            throw new RuntimeException("处理响应过滤失败", e);
        }
    }

    // 过滤Object
    @Around("@annotation(com.sft.anno.SftObjectFilter)")
    public Object SftObjectFilter(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            SftObjectFilter annotation = AnnotationUtils.findAnnotation(signature.getMethod(), SftObjectFilter.class);
            Class<?> entity = annotation.entity();
            boolean preserveField = annotation.preserveField();
            Object result = joinPoint.proceed();

            if (result != null && !entity.isAssignableFrom(result.getClass())) {
                logger.error("配置类型与实际返回类型不匹配: 配置类型 [{}], 实际返回类型 [{}]", entity.getName(), result.getClass().getName());
            }

            if (result != null) {
                if (entity.isAssignableFrom(result.getClass())) {
                    if (preserveField) {
                        SftUtil.filterFields(result);
                        return result;
                    }
                    return SftUtil.filterFieldsToMap(result, preserveField);
                }
            }
        } catch (Throwable e) {
            logger.error("处理响应过滤时发生错误", e);
            throw new RuntimeException("处理响应过滤失败", e);
        }
        return null;
    }

    private Field getField(Class<?> clazz, String fieldName) {
        // 如果没有找到，继续查找父类中的字段
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                return field;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
