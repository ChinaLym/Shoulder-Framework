package org.shoulder.autoconfigure.http;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * http（RestTemplate） 相关配置
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RestTemplate.class)
@AutoConfigureAfter(value = {RestTemplateAutoConfiguration.class, RestTemplateLogAutoConfiguration.class})
//@NotReactiveWebApplicationCondition
public class HttpAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate(@NonNull RestTemplateBuilder builder, @Nullable List<ClientHttpRequestInterceptor> interceptors) {
        if (CollectionUtils.isNotEmpty(interceptors)) {
            builder.additionalInterceptors(interceptors);
        }
        return builder.build();
    }

}
