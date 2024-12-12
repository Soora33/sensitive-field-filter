package com.sft.config;

import com.sft.aop.SftAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class SftAutoConfiguration {
    
    @Bean
    public SftAspect sftAspect() {
        return new SftAspect();
    }
}