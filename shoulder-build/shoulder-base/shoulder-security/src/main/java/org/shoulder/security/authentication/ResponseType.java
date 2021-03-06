package org.shoulder.security.authentication;

/**
 * 登录成功/失败响应类型
 *
 * @author lym
 */
public enum ResponseType {
    /**
     * 返回restful json文本，适合路由由前端控制的技术选型，如登录请求不是 form 而是 ajax 时
     */
    JSON,
    /**
     * 跳转到某个地址
     */
    REDIRECT,
    /**
     * 当浏览器请求页面时，进行请求跳转，否则返回 JSON
     */
    MIXED;

}
