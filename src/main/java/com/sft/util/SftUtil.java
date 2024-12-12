package com.sft.util;

import com.sft.anno.SftFilter;
import com.sft.aop.SftAspect;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Classname SftUtil
 * @Description
 * @Date 2024/11/14 11:05
 * @Author by Sora33
 */
public class SftUtil {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SftAspect.class);

    public static <T> void filterFields(T entity) {
        if (entity == null) {
            logger.warn("传入实体为空，跳过过滤");
            return;
        }
        // 获取需要过滤的字段
        List<String> ignoreField = getIgnoreField(entity);
        // 遍历字段，过滤掉需要忽略的字段
        ignoreField.forEach(data -> {
            try {
                Field field = entity.getClass().getDeclaredField(data);
                field.setAccessible(true);
                String value = field.getAnnotation(SftFilter.class).value();
                field.set(entity, "null".equals(value) ? null : value);
            } catch (NoSuchFieldException e) {
                logger.error("字段 【{}】 不存在", data, e);
                throw new IllegalArgumentException("字段不存在: " + data, e);
            } catch (IllegalAccessException e) {
                logger.error("无法访问字段 【{}】", data, e);
                throw new IllegalStateException("无法访问字段: " + data, e);
            }
        });
    }

    public static <T> LinkedHashMap<Object, Object> filterFieldsToMap(T entity,boolean preserveField) {
        // 保存剩余字段的 Map
        LinkedHashMap<Object, Object> resultMap = new LinkedHashMap<>();
        // 获取需要过滤的字段
        List<String> ignoreField = getIgnoreField(entity);
        // 遍历所有字段，加入 Map 中，忽略需要过滤的字段
        for (Field field : entity.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                if (!ignoreField.contains(field.getName())) {
                    resultMap.put(field.getName(), field.get(entity));
                } else if (ignoreField.contains(field.getName()) && preserveField) {
                    String value = field.getAnnotation(SftFilter.class).value();
                    resultMap.put(field.getName(), "null".equals(value) ? null : value);
                }
            } catch (IllegalAccessException e) {
                logger.error("无法访问字段: " + field.getName());
                throw new RuntimeException("无法访问字段: " + field.getName(), e);
            }
        }
        return resultMap;
    }

    private static <T> List<String> getIgnoreField(T entity) {
        Field[] fields = entity.getClass().getDeclaredFields();
        List<String> fieldsToExclude = new ArrayList<>();

        // 检查实体类字段，找出带有 @SftFilter 注解的字段
        for (Field field : fields) {
            if (field.isAnnotationPresent(SftFilter.class)) {
                fieldsToExclude.add(field.getName());
            }
        }
        return fieldsToExclude;
    }
}
