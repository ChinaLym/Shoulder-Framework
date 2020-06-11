package org.shoulder.log.operation.util;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.Operator;
import org.shoulder.log.operation.dto.SystemOperator;
import org.shoulder.log.operation.entity.OperationLogEntity;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 操作日志上下文
 * @author lym
 */
@Getter
@Setter
@Accessors(chain = true)
public class OpLogContext {

    /** 保存当前线程用户信息 */
    private static ThreadLocal<Operator> currentOperatorThreadLocal = new ThreadLocal<>();

    private OperationLogEntity logEntity;

    /** 当被操作对象为多个时，保存在这里 */
    private List<Operable> operableObjects;

    /** 触发当前业务的用户信息 */
    private Operator currentOperator;

    /** 是否在方法正常结束后自动记录日志 */
    private boolean autoLog = true;

    /** 注解所在方法抛异常后是否继续记录日志 */
    private boolean logWhenThrow = true;

    OpLogContext() {
    }

    public static Builder builder(){
        return new Builder();
    }

    public static OpLogContext create(OpLogContext lastOpLogContext){
        if(lastOpLogContext != null){
            return new OpLogContext()
                    .setCurrentOperator(lastOpLogContext.getCurrentOperator())
                    .setAutoLog(lastOpLogContext.isAutoLog())
                    .setLogWhenThrow(lastOpLogContext.isLogWhenThrow());
        } else {
            Operator operator = currentOperatorThreadLocal.get();
            return new OpLogContext()
                    .setCurrentOperator(operator != null ? operator : SystemOperator.getInstance());
        }
    }

    public static void setDefaultOperator(Operator operator) {
        currentOperatorThreadLocal.set(operator);
    }

    /**
     * 清理线程变量
     * 推荐由拦截器负责，使用者不要在业务代码中调用该方法，除非你很清楚日志框架的原理
     */
    public static void cleanDefaultOperator() {
        currentOperatorThreadLocal.remove();
    }


    public static final class Builder {
        private OperationLogEntity logEntity;
        private List<Operable> operableObjects;
        private Operator currentOperator;
        private boolean autoLog = true;
        private boolean logWhenThrow = true;

        private Builder() {
        }

        public Builder logEntity(OperationLogEntity logEntity) {
            this.logEntity = logEntity;
            return this;
        }

        public Builder operableObjects(List<Operable> operableObjects) {
            this.operableObjects = operableObjects;
            return this;
        }

        public Builder currentOperator(Operator currentOperator) {
            this.currentOperator = currentOperator;
            return this;
        }

        public Builder autoLog(Boolean autoLog) {
            this.autoLog = autoLog;
            return this;
        }

        public Builder logWhenThrow(Boolean logWhenThrow) {
            this.logWhenThrow = logWhenThrow;
            return this;
        }

        public OpLogContext build() {
            Assert.notNull(currentOperator, "currentOperator can't be null!");

            OpLogContext opLogContext = new OpLogContext();
            opLogContext.setLogEntity(logEntity);
            opLogContext.setOperableObjects(operableObjects);
            opLogContext.setCurrentOperator(currentOperator);
            opLogContext.setAutoLog(autoLog);
            opLogContext.setLogWhenThrow(logWhenThrow);
            return opLogContext;
        }
    }
}