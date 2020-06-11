package org.shoulder.log.operation.covertor;

import org.shoulder.log.operation.entity.OperationLogEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * OperationLog param.value 转换器，如需自定义或扩展推荐使用枚举继承该接口
 *
 * @author lym
 */
public interface OperationLogParamValueConverter {


    /**
     * 转换日志参数值
     * @param logEntity           预先生成的日志实体，可以从这里拿到 action 等自定义业务标识
     * @param paramValue    参数值，paramValue.getClass() 不一定等于 methodParamClazz
     * @param methodParamClazz    代码中方法描述的参数类型
     * @return 转化后的 value，允许有多个值
     */
    List<String> convert(@NonNull OperationLogEntity logEntity, @Nullable Object paramValue, Class methodParamClazz) throws Exception;

}