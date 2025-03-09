package com.shijiawei.secretblog.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@Configuration
public class SpELConfig {

    @Bean
    public SpelExpressionParser spelExpressionParser() {
        return new SpelExpressionParser();
    }

    @Bean
    public StandardEvaluationContext standardEvaluationContext() {
        return new StandardEvaluationContext();
    }

    @Bean
    public DefaultParameterNameDiscoverer defaultParameterNameDiscoverer() {
        return new DefaultParameterNameDiscoverer();
    }
} 