package com.elevn.mes.pro.mapper;

import com.elevn.mes.pro.entity.ProTaskIssue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 生产任务投料的Mapper接口
 * 对应表：pro_task_issue
 * 映射文件：resources/mapper/pro/ProTaskIssueMapper.xml
 *
 * 【谁在写这张表】
 *   目前唯一的写入方是 B 线：生产领料单（wm_doc.doc_type = 'ISSUE'）过账成功后，
 *   WmDocServiceImpl 会把每一行领料翻译成一条投料记录。
 *   为什么不是 A 线自己写？因为"货从库里出去了"这件事只有过账那一刻才成立，
 *   由发起方（仓储过账）在同一事务里写，才能保证"库存扣了、投料也记了"，不会一半一半。
 *
 * 【投入量为什么要校验】
 *   同一张领料单只允许产生一批投料记录。过账本身有 status='PREPARE' 卡住重复执行，
 *   这里再留一个 countBySourceDocId 做兜底，双保险。
 *
 */
@Mapper
public interface ProTaskIssueMapper {

    /**
     * 新增投料记录（主键回填到 recordId）
     * @param taskIssue 投料对象
     * @return 影响行数
     */
    int insert(ProTaskIssue taskIssue);

    /**
     * 按生产任务查询投料记录（任务详情页展示"这个任务领了哪些料"）
     * @param taskId 生产任务ID
     * @return 投料记录列表
     */
    List<ProTaskIssue> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 统计某来源单据已经产生了多少条投料记录（防重复过账的兜底校验）
     * @param sourceDocTable 来源单据表名
     * @param sourceDocId 来源单据ID
     * @return 记录数
     */
    int countBySourceDocId(@Param("sourceDocTable") String sourceDocTable,
                           @Param("sourceDocId") Long sourceDocId);
}
