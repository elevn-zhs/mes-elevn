package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmBarcode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 条码 Mapper
 *
 */
public interface WmBarcodeMapper {

    /** 多条件分页查询 */
    List<WmBarcode> selectByCondition(WmBarcode query);

    /** 按主键查询 */
    WmBarcode selectById(@Param("barcodeId") Long barcodeId);

    /** 按内容精确查（扫码解析的唯一入口） */
    WmBarcode selectByContent(@Param("barcodeContent") String barcodeContent);

    /** 内容是否已存在 */
    int countByContent(@Param("barcodeContent") String barcodeContent,
                       @Param("excludeId") Long excludeId);

    /** 新增 */
    int insert(WmBarcode barcode);

    /** 回填图片路径 */
    int updateBarcodeUrl(@Param("barcodeId") Long barcodeId,
                         @Param("barcodeUrl") String barcodeUrl);

    /** 逻辑删除 */
    int deleteById(@Param("barcodeId") Long barcodeId);
}
