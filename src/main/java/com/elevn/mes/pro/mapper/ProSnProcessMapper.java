package com.elevn.mes.pro.mapper;

import com.elevn.mes.pro.entity.ProSnProcess;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * SN 过站记录 Mapper（B 线 SN 追溯只读此表，写入方是 A 线报工）
 *
 */
public interface ProSnProcessMapper {

    /** 按 SN 查全部过站记录（时间正序 = 按 seq_num 排） */
    List<ProSnProcess> selectBySnId(@Param("snId") Long snId);
}
