package com.sft.config;

/**
 * @Description Sft配置类
 */
public class SftConfig {

    // 默认非严格模式
    private static boolean strictTypeChecking = false;
    
    public static boolean isStrictTypeChecking() {
        return strictTypeChecking;
    }
    
    public static void setStrictTypeChecking(boolean strictTypeChecking) {
        SftConfig.strictTypeChecking = strictTypeChecking;
    }
}