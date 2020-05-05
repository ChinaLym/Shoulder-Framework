package org.shoulder.log.operation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * 操作日志： 被操作对象
 * 【推荐】每个 组件/模块 的被操作对象实现该接口
 * @author lym
 */
public interface Operable extends ActionDetailAble {
    
    // ============================ 全部可空，如果无对应，返回null即可 ===============================
    
    /**
     * 被操作对象唯一标识
     * @return 返回业日志操作实体的唯一标识
     */
    @JsonIgnore
    String getObjectId();

    /**
     * 被操作对象名称
     * @return 返回业日志操作实体的名称
     */
    @JsonIgnore
    String getObjectName();

    /**
     * 被操作对象类型多语言key
     * @return 返回业日志操作实体的对象类型
     */
    @JsonIgnore
    default String getObjectType(){
        return null;
    }

    /**
     * 操作日志的 actionDetail 字段，由于批量日志中 易变的部分包括 被操作对象 和 actionDetail
     *      且操作详情通常是根据操作对象生成的，为了使用者方便，特集成在这里。
     *
     * @implSpec 但并不是所有时候都能通过可操作对象生成操作日志的 actionDetail，且这是后来版本增加的接口，不能强制已使用者实现，因此默认为不能生成 actionDetail
     *
     * */
    @Override
    @JsonIgnore
    default List<String> getActionDetail(){
        return LOG_DETAIL_IGNORE;
    }

}
