package com.atguigu.aop2;

import org.springframework.stereotype.Service;

@Service
public class ProductService {
    // 通过参数名引用
    @RedisCache(key = "'product:' + #id", expire = 600)
    public String getProductById(Long id) {
        // 查询数据库
        System.out.println("查询数据库");
        return "product===" + id;
    }

    // 支持复杂表达式
    @RedisCache(key = "'user_product:' + #userId + '_' + #productId")
    public String getUserProduct(Long userId, Long productId) {
        System.out.println(String.format("查询数据库，userId=%d, productId=%d", userId, productId));
        return "user_product====" + userId + "_" + productId;
    }

    @CacheEvict(key = "'product:' + #id")
    public void updateProduct(Long id, String product) {
        // 更新数据库
        System.out.println("更新数据库");
    }
}