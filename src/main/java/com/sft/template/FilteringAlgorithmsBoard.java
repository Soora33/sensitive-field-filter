package com.sft.template;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;

/**
 * @Classname FilteringAlgorithmsBoard
 * @Description 过滤算法模板
 * @Date 2024/12/13 14:27
 * @Author by Sora33
 */
public abstract class FilteringAlgorithmsBoard {

    private static final Logger logger = LoggerFactory.getLogger(ObjectFilterTemplate.class);
    protected ProceedingJoinPoint joinPoint;
    protected Object methodResult;

    public FilteringAlgorithmsBoard(Object methodResult, ProceedingJoinPoint joinPoint) {
        this.joinPoint = joinPoint;
        try {
            this.methodResult = methodResult;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public final Object process() {
        // 获取方法注解
        Annotation annotation = getMethodAnno();
        // 判断是否需要保留字段
        boolean preserveField = preserveField(annotation);
        // 校验返回值合理性
        if (!configureVerification()) {
            return methodResult;
        }
        // 判断是否需要保留字段
        if (preserveField) {
            return handleResultPreserveField();
        } else {
            return handleResult();
        }
    }

    // 方法注解
    protected abstract Annotation getMethodAnno();
    // 是否需要保留字段
    protected abstract boolean preserveField(Annotation annotation);
    // 保留字段
    protected abstract Object handleResultPreserveField();
    // 不保留字段
    protected abstract Object handleResult();
    // 规则校验
    protected abstract boolean configureVerification();
    // 返回值类型校验
    protected abstract boolean isTypeMatched(Object methodObject);
    // 返回类型不匹配日志
    protected abstract void logTypeMismatch(Object methodObject);
    // 返回为空日志
    protected abstract void logNullResult();
}
