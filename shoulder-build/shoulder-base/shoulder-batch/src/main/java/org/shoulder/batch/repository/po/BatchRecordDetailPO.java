package org.shoulder.batch.repository.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.shoulder.batch.model.BatchRecordDetail;

import java.io.Serializable;

/**
 * 批量处理记录详情-标准模型
 *
 * @author lym
 */
@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
public class BatchRecordDetailPO implements Serializable {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 批量处理记录表id
     */
    private String recordId;

    /**
     * 本次批处理所在位置行号 / 索引 / 下标
     */
    private int index;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 处理结果状态 0 处理成功 1 校验失败、2 重复跳过、3 重复更新、4 处理失败
     */
    private int status;

    /**
     * 失败原因，推荐支持多语言
     */
    private String failReason;

    /**
     * 处理的原始数据
     */
    private String source;

    public BatchRecordDetailPO() {
    }

    public BatchRecordDetailPO(int index, int status) {
        this.index = index;
        this.status = status;
    }

    public BatchRecordDetailPO(int index, int status, String failReason) {
        this.index = index;
        this.status = status;
        this.failReason = failReason;
    }


    public BatchRecordDetailPO(BatchRecordDetail model) {
        id = model.getId();
        recordId = model.getRecordId();
        index = model.getIndex();
        operation = model.getOperation();
        status = model.getStatus();
        failReason = model.getFailReason();
        source = model.getSource();
    }

    public BatchRecordDetail toModel() {
        return BatchRecordDetail.builder()
            .id(id)
            .recordId(recordId)
            .index(index)
            .operation(operation)
            .operation(operation)
            .status(status)
            .failReason(failReason)
            .source(source)
            .build();
    }


}
