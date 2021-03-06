package org.shoulder.security.code.sms;

/**
 * 短信发送
 * 这里仅向控制台打印，实际中一般会调用消息推送服务，向目标手机号发送短信
 * 如何覆盖该实现？自行实现 SmsCodeSender 接口注入到 spring 中即可。
 *
 * @author lym
 */
public class MockSmsCodeSender implements SmsCodeSender {

    @Override
    public void send(String mobile, String code) {
        // 实际短信这种资源需要限流，如相同手机号（相同业务）每分钟只能发起一次请求
        System.out.println("假装向手机 " + mobile + " 发送短信验证码 " + code);
    }

}
