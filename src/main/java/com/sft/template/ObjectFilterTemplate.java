package com.sft.template;

import com.sft.anno.SftObjectFilter;
import com.sft.config.SftConfig;
import com.sft.util.SftUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @Classname ObjectFilterTemplate
 * @Description 对象模板
 * @Date 2024/12/13 14:51
 * @Author by Sora33
 */
public class ObjectFilterTemplate extends FilteringAlgorithmsBoard{

    private static final Logger logger = LoggerFactory.getLogger(ObjectFilterTemplate.class);

    private Class<?> entity;
    private boolean preserveField;

    public ObjectFilterTemplate(Object methodResult, ProceedingJoinPoint joinPoint) {
        super(methodResult,joinPoint);
    }

    @Override
    protected Annotation getMethodAnno() {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return AnnotationUtils.findAnnotation(signature.getMethod(), SftObjectFilter.class);
    }

    @Override
    protected boolean preserveField(Annotation annotation) {
        SftObjectFilter filter = (SftObjectFilter) annotation;
        this.entity = filter.entity();
        this.preserveField = filter.preserveField();
        return filter.preserveField();
    }

    @Override
    protected Object handleResultPreserveField() {
        try {
            return Optional.ofNullable(methodResult)
                    .map(methodResult -> {
                        if (methodResult instanceof Collection) {
                            Collection<?> collection = (Collection<?>) methodResult;
                            for (Object item : collection) {
                                if (item != null) {
                                    SftUtil.filterFields(item);
                                }
                            }
                        } else {
                            SftUtil.filterFields(methodResult);
                        }
                        return methodResult;
                    }).orElseGet(() -> {
                        logNullResult();
                        return methodResult;
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
                        if (result instanceof Collection) {
                            Collection<?> collection = (Collection<?>) result;
                            // 使用与原集合相同类型的集合
                            Collection<Object> filteredCollection = createSameTypeCollection(collection);
                            for (Object item : collection) {
                                if (item != null) {
                                    filteredCollection.add(SftUtil.filterFieldsToMap(item, this.preserveField));
                                }
                            }
                            return filteredCollection;
                        }
                        return SftUtil.filterFieldsToMap(result, this.preserveField);
                    })
                    .orElseGet(() -> {
                        logNullResult();
                        return methodResult;
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
        if (methodResult == null) {
            logNullResult();
            return false;
        }

        // 处理集合类型
        if (methodResult instanceof Collection) {
            Collection<?> collection = (Collection<?>) methodResult;
            // 空集合视为匹配
            if (collection.isEmpty()) {
                return true;
            }
            // 检查集合中第一个非空元素的类型
            return collection.stream()
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(item -> this.entity.isAssignableFrom(item.getClass()))
                    .orElse(true);
        }

        return this.entity.isAssignableFrom(methodResult.getClass());
    }

    @Override
    protected void logTypeMismatch(Object methodResult) {
        if (SftConfig.isStrictTypeChecking()) {
            throw new RuntimeException("响应类型不匹配: 配置类型 【" + this.entity.getName() + "】, " +
                    "实际类型 【" + methodResult.getClass().getName() + "】");
        } else {
            logger.error("响应类型不匹配: 配置类型 【{}】, 实际类型 【{}】",
                    this.entity.getName(), methodResult.getClass().getName());
        }
    }

    @Override
    protected void logNullResult() {
        logger.error("返回值为 null: 配置类型 【{}】", this.entity.getName());
    }

    // 创建与原集合相同类型的新集合
    private Collection<Object> createSameTypeCollection(Collection<?> original) {
        if (original instanceof List) {
            return new ArrayList<>();
        } else if (original instanceof Set) {
            return new HashSet<>();
        }
        // 默认使用ArrayList
        return new ArrayList<>();
    }
}
