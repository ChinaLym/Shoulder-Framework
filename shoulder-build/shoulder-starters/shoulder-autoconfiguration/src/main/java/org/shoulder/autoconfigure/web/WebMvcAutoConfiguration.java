package org.shoulder.autoconfigure.web;

import org.shoulder.web.interceptor.BaseRejectRepeatSubmitInterceptor;
import org.shoulder.web.interceptor.HttpLocaleInterceptor;
import org.shoulder.web.interceptor.SessionTokenRepeatSubmitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcAutoConfiguration
 *
 * @author lym
 */
@Configuration
@ConditionalOnWebApplication
public class WebMvcAutoConfiguration {

    @Configuration
    @ConditionalOnClass(HttpLocaleInterceptor.class)
    protected static class LocaleInterceptorWebConfig implements WebMvcConfigurer {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new HttpLocaleInterceptor()).order(Ordered.HIGHEST_PRECEDENCE);
            WebMvcConfigurer.super.addInterceptors(registry);
        }
    }

    @Configuration
    @ConditionalOnClass(SessionTokenRepeatSubmitInterceptor.class)
    @ConditionalOnProperty(name = "shoulder.web.repeatSubmit.enable", havingValue = "true", matchIfMissing = true)
    protected static class RejectRepeatSubmitWebConfig implements WebMvcConfigurer {

        @Autowired
        private BaseRejectRepeatSubmitInterceptor rejectRepeatSubmitInterceptor;

        @Bean
        @ConditionalOnMissingBean(BaseRejectRepeatSubmitInterceptor.class)
        public SessionTokenRepeatSubmitInterceptor rejectRepeatSubmitInterceptor(
            @Value("${shoulder.web.waf.repeatSubmit.requestTokenName:__repeat_token}") String requestTokenName,
            @Value("${shoulder.web.waf.repeatSubmit.sessionTokenName:__repeat_token}") String sessionTokenName
        ){
            return new SessionTokenRepeatSubmitInterceptor(requestTokenName, sessionTokenName);
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(rejectRepeatSubmitInterceptor).order(Ordered.HIGHEST_PRECEDENCE);
            WebMvcConfigurer.super.addInterceptors(registry);
        }
    }

}