package org.shoulder.autoconfigure.log.operation;

import lombok.extern.shoulder.SLog;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLogConfig;
import org.shoulder.log.operation.annotation.OperationLogParam;
import org.shoulder.log.operation.covertor.DefaultOperationLogParamValueConverter;
import org.shoulder.log.operation.covertor.OperationLogParamValueConverter;
import org.shoulder.log.operation.covertor.OperationLogParamValueConverterHolder;
import org.shoulder.log.operation.entity.ActionParam;
import org.shoulder.log.operation.entity.OperationLogEntity;
import org.shoulder.log.operation.util.OpLogContext;
import org.shoulder.log.operation.util.OpLogContextHolder;
import org.shoulder.log.operation.util.OperationLogBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


/**
 * 激活操作日志 OperationLog 注解 AOP
 *
 * @author lym
 */
@SLog
@Aspect
@Configuration
@ConditionalOnClass(OperationLog.class)
@AutoConfigureAfter(value = {
        OperationLoggerAutoConfiguration.class,
        OperationLogParamConverterAutoConfiguration.class
})
@EnableConfigurationProperties(OperationLogProperties.class)
public class OperationLogAspect {

    @Autowired
    private OperationLogProperties operationLogProperties;

    /**
     * 用于SpEL表达式解析.
     */
    private SpelExpressionParser parser = new SpelExpressionParser();

    //用于获取方法参数定义名字.
    //private DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();


    /**
     * 保存操作日志上次的上下文
     */
    private static ThreadLocal<OpLogContext> lastOpLogContext = new ThreadLocal<>();

    // ********************************* annotation AOP *********************************************

    /**
     * 标识加了 OperationLog 注解的方法
     */
    @Pointcut("@annotation(org.shoulder.log.operation.annotation.OperationLog)")
    public void methodAnnotatedByOperationLog() {
    }

    @Around("methodAnnotatedByOperationLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {

        // 保存之前的日志上下文信息
        stashLastContext();

        // 创建日志上下文
        creatNewContext(joinPoint);

        // ---------------- 执行注解所在目标方法  ------------
        Object o = joinPoint.proceed();
        // ---------------- 注解所在方法正常反回后 -----------

        log.debug("OperationLogUtils.autoLog={}", OpLogContextHolder.isEnableAutoLog());

        // 方法执行后： 记录日志 并清除 threadLocal
        if (OpLogContextHolder.isEnableAutoLog()) {
            OpLogContextHolder.log();
        }

        // 恢复之前的上下文
        popLastContext();
        return o;
    }

    /**
     * 注解所在方法抛异常后
     * 1. 记录日志
     * 2. 清除 threadLocal
     */
    @AfterThrowing(pointcut = "methodAnnotatedByOperationLog()")
    public void doAfterThrowing() {
        if (OpLogContextHolder.isEnableAutoLog() && OpLogContextHolder.isLogWhenThrow()) {
            OpLogContextHolder.getLog().setResultFail();
            OpLogContextHolder.log();
        }

        // 恢复之前的上下文
        popLastContext();
    }

    // ************************************* 私有方法 ***********************************

    /**
     * 进方法前存储的之前的日志上下文
     */
    private void stashLastContext(){
        lastOpLogContext.set(OpLogContextHolder.getContext());
    }

    private void creatNewContext(ProceedingJoinPoint joinPoint){
        // 解析注解
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        OperationLog methodAnnotation = method.getAnnotation(OperationLog.class);
        OperationLogConfig classAnnotation = method.getDeclaringClass().getAnnotation(OperationLogConfig.class);
        if (methodAnnotation == null) {
            // 不可能的情况，因为日志 AOP 就是以该注解为切点
            throw new IllegalStateException("@OperationLog can't be null.");
        }

        // 创建日志
        OperationLogEntity entity = createLog(joinPoint, methodAnnotation, classAnnotation);

        if (log.isDebugEnabled()) {
            log.debug("auto create a OperationLog: " + entity);
        }

        OpLogContext context = OpLogContext.create(lastOpLogContext.get()).setLogEntity(entity);

        // 是否在抛出异常后自动记录日志

        OpLogContextHolder.setContext(context);
    }



    /**
     * 执行方法后，清理本方法的上下文，恢复上次的上下文
     */
    private void popLastContext(){
        OpLogContext lastContext = lastOpLogContext.get();
        if(lastContext != null) {
            OpLogContextHolder.setContext(lastContext);
        } else {
            OpLogContextHolder.clean();
        }
        lastOpLogContext.remove();
    }

    /**
     * 根据注解创建日志实体
     */
    @NonNull
    private OperationLogEntity createLog(ProceedingJoinPoint joinPoint, OperationLog methodAnnotation, OperationLogConfig classAnnotation) {
        // 创建日志实体
        OperationLogEntity entity =
                OperationLogBuilder.newLog(methodAnnotation.action());

        // objectType
        if (StringUtils.isNotEmpty(methodAnnotation.objectType())) {
            entity.setObjectType(methodAnnotation.objectType());
        } else if (classAnnotation != null && StringUtils.isNotEmpty(classAnnotation.objectType())) {
            entity.setObjectType(classAnnotation.objectType());
        }

        // terminalType
        if (StringUtils.isNotEmpty(methodAnnotation.terminalType())) {
            entity.setTerminalType(methodAnnotation.terminalType());
        }

        //多语言、i18nKey、actionDetail
        String i18nKey = methodAnnotation.i18nKey();
        String actionDetail = methodAnnotation.actionDetail();

        if (StringUtils.isNotBlank(i18nKey)) {
            // 填写 i18nKey 代表支持多语言
            entity.setDetailI18nKey(i18nKey);
        }
        if (StringUtils.isNotEmpty(actionDetail)) {
            // 填写 actionDetail 不填写 i18nKey 认为不支持多语言
            entity.setDetail(actionDetail);
        }

        // 解析日志参数
        entity.setActionParams(createActionParams(entity, joinPoint));

        return entity;
    }


    /**
     * 解析 actionParams
     *
     * @param entity 解析过的日志实体
     * @param joinPoint 连接点
     * @return 本方法中要记录的参数
     */
    private List<ActionParam> createActionParams(@NonNull OperationLogEntity entity, ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        List<ActionParam> actionParams = new LinkedList<>();
        Parameter[] parameters = method.getParameters();
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        OperationLog methodAnnotation = method.getAnnotation(OperationLog.class);
        for (int i = 0; i < parameters.length; i++) {
            OperationLogParam paramAnnotation = parameters[i].getAnnotation(OperationLogParam.class);
            // if args[n] == null or without OperationLogParam Annotation than continue.
            // todo 解析 OperationLog
            if (paramAnnotation == null && !methodAnnotation.logAllParam()) {
                continue;
            }
            ActionParam actionParam = new ActionParam();

            String name = parameterNames[i];
            boolean supportI18n = false;
            String valueSPEL = "";
            Class<? extends OperationLogParamValueConverter> converterClazz =
                    DefaultOperationLogParamValueConverter.class;

            if (paramAnnotation != null) {
                if (StringUtils.isNotBlank(paramAnnotation.name())) {
                    name = paramAnnotation.name();
                }
                supportI18n = paramAnnotation.supportI18n();
                valueSPEL = paramAnnotation.value();
                converterClazz = paramAnnotation.converter();
            }

            actionParam.setName(name);
            actionParam.setI18nValue(supportI18n);
            // setValue
            try {
                if (StringUtils.isNotBlank(valueSPEL)) {
                    // 使用 spel -> value
                    Expression expression = parser.parseExpression(valueSPEL);
                    EvaluationContext context = new StandardEvaluationContext();
                    if(args[i] == null){
                        actionParam.setValue(Collections.singletonList(operationLogProperties.getNullParamOutput()));
                    } else {
                        context.setVariable(parameterNames[i], args[i]);
                        //for (int i = 0; i < args.length; i++) { }
                        actionParam.setValue(Collections.singletonList(Objects.requireNonNull(
                                expression.getValue(context)).toString()));
                    }

                } else {
                    // 使用 converter
                    OperationLogParamValueConverter converter = OperationLogParamValueConverterHolder.getConvert(converterClazz);

                    actionParam.setValue(converter.convert(entity, args[i], parameters[i].getType()));

                }
            } catch (Exception e) {
                log.info("try convert FAIL, but ignored by replace with default value(null). class:'" +
                        method.getDeclaringClass().getName() +
                        "', method:'" + method.getName() +
                        "', paramName=" + parameterNames[i], e);
                // 忽略该参数
                continue;
            }
            actionParams.add(actionParam);
        }
        return actionParams;
    }

}