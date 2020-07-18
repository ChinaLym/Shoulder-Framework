package org.shoulder.core.converter;

import org.springframework.lang.NonNull;

/**
 * String 转枚举失败时处理器，返回 null
 * @author lym
 */
public class DefaultEnumMissMatchHandler implements EnumMissMatchHandler {

    //private final String defaultFieldName = "default";

    //private final String defaultMethodName = "default";

    @Override
    public <T> T handleNullSource(@NonNull Class<? extends Enum> enumType) {
        return null;
    }

    /**
     * 默认直接返回 null，使用者可以根据自行的编码风格进行特定处理，如：
     *
     *  尝试反射调用 public static T of(String source) 方法
     *  尝试使用标识字段匹配，如统一比对 code 字段是否匹配
     *  尝试使用标识方法匹配，如统一比对 getCode 方法是否匹配
     */
    @Override
    public <T> T handleMissMatch(@NonNull Class<? extends Enum> enumType, @NonNull String source) {
        return null;
    }

}