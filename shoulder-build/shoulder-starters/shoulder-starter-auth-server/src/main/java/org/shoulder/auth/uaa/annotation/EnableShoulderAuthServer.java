package org.shoulder.auth.uaa.annotation;

import org.shoulder.auth.uaa.configuration.ShoulderAuthServerConfigurationMark;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 标记一个应用为认证服务器
 *
 * @author lym
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ShoulderAuthServerConfigurationMark.class)
public @interface EnableShoulderAuthServer {

}
