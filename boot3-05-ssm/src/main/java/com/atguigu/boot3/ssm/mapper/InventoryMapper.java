package com.atguigu.boot3.ssm.mapper;

import com.atguigu.boot3.ssm.bean.Inventory;
import org.apache.ibatis.annotations.Param;

/**
 * 库存 Mapper（方法二：悲观锁）
 */
public interface InventoryMapper {

    /**
     * SELECT ... FOR UPDATE 行级悲观锁查询
     *
     * @param id 商品 ID
     * @return 库存记录
     */
    Inventory selectForUpdate(@Param("id") Long id);

    /**
     * 扣减库存
     *
     * @param id       商品 ID
     * @param newStock 新库存值
     * @return 更新行数
     */
    int updateStock(@Param("id") Long id, @Param("newStock") int newStock);
}
