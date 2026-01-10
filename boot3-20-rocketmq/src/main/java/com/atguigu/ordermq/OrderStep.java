package com.atguigu.ordermq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单步骤实体类
 * 用于演示顺序消息和非顺序消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStep {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单步骤描述
     * 例如：创建订单、付款、推送、完成
     */
    private String desc;

    @Override
    public String toString() {
        return "OrderStep{" +
                "orderId=" + orderId +
                ", desc='" + desc + '\'' +
                '}';
    }
}
