package org.shoulder.autoconfigure.log.operation;

import org.shoulder.core.constant.ShoulderFramework;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for operationLog.
 *
 * @author lym
 */
@ConfigurationProperties(prefix = ShoulderFramework.CONFIG_PREFIX + "log.operation")
public class OperationLogProperties {

    /**
     * 是否以异步线程记录 操作日志.
     */
    private boolean async = true;

    /**
     * 用于异步记录日志的线程数
     */
    private Integer threadNum = 1;

    /**
     * 异步记录日志线程的名称
     */
    private String threadName = "shoulder-async-operation-logger";

    /**
     * 参数值为 null 时的输出样式，一般取值为 '' 或 'null' 等
     */
    private String nullParamOutput = "";

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public Integer getThreadNum() {
        return threadNum != null && threadNum > 0 ? threadNum : 1;
    }

    public void setThreadNum(Integer threadNum) {
        this.threadNum = threadNum;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getNullParamOutput() {
        return nullParamOutput;
    }

    public void setNullParamOutput(String nullParamOutput) {
        this.nullParamOutput = nullParamOutput;
    }

}
