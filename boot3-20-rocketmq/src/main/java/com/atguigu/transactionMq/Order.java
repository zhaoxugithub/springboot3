package com.atguigu.transactionMq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单实体类
 * 用于演示事务消息场景：当订单创建成功后，发送消息到下游系统（如库存系统、积分系统）
 * 
 * @author atguigu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {
    
    /**
     * 订单ID
     */
    private String orderId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 商品ID
     */
    private String productId;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 订单金额
     */
    private BigDecimal amount;
    
    /**
     * 购买数量
     */
    private Integer quantity;
    
    /**
     * 订单状态：0-待支付，1-已支付，2-已取消
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
}
