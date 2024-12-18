package com.sft.template;

import com.sft.anno.SftResponseFilter;
import com.sft.config.SftConfig;
import com.sft.util.SftUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @Classname ResponseFilterTemplate
 * @Description 响应数据过滤模板
 * @Author by Sora33
 */
public class ResponseFilterTemplate extends FilteringAlgorithmsBoard {

    private static final Logger logger = LoggerFactory.getLogger(ResponseFilterTemplate.class);

    private Class<?> entity;
    private String key;
    private boolean preserveField;

    public ResponseFilterTemplate(Object methodResult, ProceedingJoinPoint joinPoint) {
        super(methodResult, joinPoint);
    }

    @Override
    protected Annotation getMethodAnno() {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return AnnotationUtils.findAnnotation(signature.getMethod(), SftResponseFilter.class);
    }

    @Override
    protected boolean preserveField(Annotation annotation) {
        SftResponseFilter filter = (SftResponseFilter) annotation;
        this.entity = filter.entity();
        this.key = filter.key();
        this.preserveField = filter.preserveField();
        return filter.preserveField();
    }

    @Override
    protected Object handleResultPreserveField() {
        try {
            return Optional.ofNullable(methodResult)
                    .map(result -> {
                        Object value = extractValue(result);
                        if (value instanceof Collection) {
                            Collection<?> collection = (Collection<?>) value;
                            for (Object item : collection) {
                                if (item != null) {
                                    SftUtil.filterFields(item);
                                }
                            }
                        } else if (value != null) {
                            SftUtil.filterFields(value);
                        }
                        return result;
                    })
                    .orElseGet(() -> {
                        logNullResult();
                        return null;
                    });
        } catch (Throwable e) {
            logger.error("处理响应过滤时发生错误", e);
            throw new RuntimeException("处理响应过滤失败", e);
        }
    }

    @Override
    protected Object handleResult() {
        try {
            return Optional.ofNullable(methodResult)
                    .map(result -> {
                        Object value = extractValue(result);
                        if (value instanceof Collection) {
                            Collection<?> collection = (Collection<?>) value;
                            Collection<Object> filteredCollection = createSameTypeCollection(collection);
                            for (Object item : collection) {
                                if (item != null) {
                                    filteredCollection.add(SftUtil.filterFieldsToMap(item, this.preserveField));
                                }
                            }
                            updateValue(result, filteredCollection);
                        } else if (value != null) {
                            updateValue(result, SftUtil.filterFieldsToMap(value, this.preserveField));
                        }
                        return result;
                    })
                    .orElseGet(() -> {
                        logNullResult();
                        return null;
                    });
        } catch (Throwable e) {
            logger.error("处理响应过滤时发生错误", e);
            throw new RuntimeException("处理响应过滤失败", e);
        }
    }

    @Override
    protected boolean configureVerification() {
        if (isTypeMatched(methodResult)) {
            return true;
        }
        logTypeMismatch(methodResult);
        return false;
    }

    @Override
    protected boolean isTypeMatched(Object methodResult) {
        // 获取核心数据值
        Object value = extractValue(methodResult);

        if (value == null) {
            return false;
        }

        // 如果集合为空则通过
        if (value instanceof Collection) {
            if (((Collection<?>) value).isEmpty()) {
                return true;
            }
        }

        // 返回值类型校验
        if (!this.entity.isAssignableFrom(methodResult.getClass())) {
            return false;
        }

        // 单个对象直接通过
        return true;
    }

    @Override
    protected void logTypeMismatch(Object methodResult) {
        if (methodResult == null) {
            logNullResult();
            return;
        }

        if (!this.entity.isAssignableFrom(methodResult.getClass())) {
            if (SftConfig.isStrictTypeChecking()) {
                throw new RuntimeException("响应类型不匹配: 配置类型 【" + this.entity.getName() + "】, " +
                        "实际类型 【" + methodResult.getClass().getName() + "】");
            } else {
                logger.error("响应类型不匹配: 配置类型 【{}】, 实际类型 【{}】",
                        this.entity.getName(), methodResult.getClass().getName());
            }

        }
    }

    @Override
    protected void logNullResult() {
        logger.error("返回值为 null: 配置类型 【{}】", this.entity.getName());
    }

    // 从返回结果中提取需要处理的值
    private Object extractValue(Object result) {
        if (result instanceof Map) {
            Object keyData = ((Map<?, ?>) result).get(key);
            if (keyData != null) {
                return keyData;
            }
            if (SftConfig.isStrictTypeChecking()) {
                throw new RuntimeException("根据 key：【" + key + "】 获取数据失败，未找到字段");
            } else {
                logger.error("根据 key：【{}】 获取数据失败，未找到字段", key);
            }
        }

        try {
            Field field = getField(result.getClass(), key);
            if (field != null) {
                field.setAccessible(true);
                return field.get(result);
            }
        } catch (Exception e) {
            logger.error("获取字段值失败", e);
        }
        if (SftConfig.isStrictTypeChecking()) {
            throw new RuntimeException("根据 key：【" + key + "】 获取数据失败，未找到字段");
        } else {
            logger.error("根据 key：【{}】 获取数据失败，未找到字段", key);
        }
        return null;
    }

    // 更新返回结果中的值
    private void updateValue(Object result, Object newValue) {
        if (result instanceof Map) {
            ((Map<Object, Object>) result).put(key, newValue);
            return;
        }

        try {
            Field field = getField(result.getClass(), key);
            if (field != null) {
                field.setAccessible(true);
                field.set(result, newValue);
            }
        } catch (Exception e) {
            logger.error("更新字段值失败", e);
        }
    }

    // 创建与原集合相同类型的新集合
    private Collection<Object> createSameTypeCollection(Collection<?> original) {
        if (original instanceof List) {
            return new ArrayList<>();
        } else if (original instanceof Set) {
            return new HashSet<>();
        }
        return new ArrayList<>();
    }

    // 获取字段（包括父类字段）
    private Field getField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}