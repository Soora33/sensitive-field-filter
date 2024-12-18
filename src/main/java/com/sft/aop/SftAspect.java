package com.sft.aop;

import com.sft.template.ObjectFilterTemplate;
import com.sft.template.ResponseFilterTemplate;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@Aspect
@Component
@EnableAspectJAutoProxy
public class SftAspect {

    private static final Logger logger = LoggerFactory.getLogger(SftAspect.class);

    // 过滤Response
    @Around("@annotation(com.sft.anno.SftResponseFilter)")
    public Object SftResponseFilter(ProceedingJoinPoint joinPoint) {
        try {
            Object result = joinPoint.proceed();
            ResponseFilterTemplate template = new ResponseFilterTemplate(result,joinPoint);
            return template.process();
        } catch (Throwable e) {
            throw new RuntimeException("处理响应过滤失败", e);
        }
    }

    // 过滤Object
    @Around("@annotation(com.sft.anno.SftObjectFilter)")
    public Object SftObjectFilter(ProceedingJoinPoint joinPoint) {
        try {
            Object result = joinPoint.proceed();
            ObjectFilterTemplate template = new ObjectFilterTemplate(result,joinPoint);
            return template.process();
        } catch (Throwable e) {
            throw new RuntimeException("处理响应过滤失败", e);
        }
    }
}
