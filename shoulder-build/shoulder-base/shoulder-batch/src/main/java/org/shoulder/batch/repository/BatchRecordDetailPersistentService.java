package org.shoulder.batch.repository;

import org.shoulder.batch.repository.po.BatchRecordDetailPO;

import java.util.List;

/**
 * 批处理记录持久化接口
 *
 * @author lym
 */
public interface BatchRecordDetailPersistentService {

    /**
     * 批量新增处理详情
     *
     * @param batchRecordDetailList 要插入的记录
     */
    void batchInsertRecordDetail(List<BatchRecordDetailPO> batchRecordDetailList);

    /**
     * 查询所有的批量处理记录
     *
     * @param recordId   记录标识
     * @param resultList 结果状态
     * @return 所有的批量处理记录
     */
    List<BatchRecordDetailPO> findAllByResult(String recordId, List<Integer> resultList);


}
