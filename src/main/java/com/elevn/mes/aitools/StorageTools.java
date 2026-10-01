package com.elevn.mes.aitools;

import com.elevn.mes.md.entity.MdItem;
import com.elevn.mes.md.entity.MdItemType;
import com.elevn.mes.md.service.MdItemService;
import com.elevn.mes.md.service.MdItemTypeService;
import com.elevn.mes.wm.entity.WmMaterialStock;
import com.elevn.mes.wm.entity.WmWarehouse;
import com.elevn.mes.wm.service.WmStockService;
import com.elevn.mes.wm.service.WmWarehouseService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StorageTools {
    @Autowired
    private MdItemService itemService;
    @Autowired
    private WmStockService stockService;
    @Autowired
    private MdItemTypeService itemTypeService;
    @Autowired
    private WmWarehouseService warehouseService;


    @Tool(description = "根据物料的名称模糊查询物料列表")
    public List<MdItem> getItemList(@ToolParam(description = "物料的名称，用来模糊查询；比如：电阻")String itemName){
        return itemService.queryByName(itemName);
    }

    @Tool(description = "查询所有的物料或者产品分类，查询的结果是树结构，根据itemOrProduct来区分是物料还是产品")
    public List<MdItemType> getAllType(){
        return itemTypeService.queryByParentId(0L);
    }

    @Tool(description = "根据仓库的ID和物料的ID进行统计的方法，得到统计列表")
    public List<WmMaterialStock> summaryByWarehouseAndItem(
            @ToolParam(description = "仓库的ID")Long warehouseId,
            @ToolParam(description = "物料的ID")Long itemId
    ){
        return stockService.summaryByWarehouseAndItem(warehouseId,itemId);
    }

    @Tool(description = "查询所有的仓库信息")
    public List<WmWarehouse> getAllWareHouse(){
        return warehouseService.queryAllEnabled();
    }
}
