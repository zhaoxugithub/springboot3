package com.atguigu.boot.lifecycle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean 生命周期配置类
 *
 * 通过 @Bean 注解配置 Bean，并指定：
 * - initMethod: 自定义初始化方法
 * - destroyMethod: 自定义销毁方法
 *
 * 这种方式的优点：
 * 1. 解耦：不需要 Bean 实现 Spring 接口
 * 2. 灵活：可以为第三方类配置生命周期方法
 * 3. 清晰：初始化和销毁逻辑一目了然
 */
@Configuration
public class LifecycleConfig {

    /**
     * 注册 LifecycleBean 到 Spring 容器
     *
     * @Bean 注解的属性：
     * - name/value: 指定 Bean 的名称，默认是方法名
     * - initMethod: 指定初始化方法名（Bean 中必须存在该方法）
     * - destroyMethod: 指定销毁方法名（Bean 中必须存在该方法）
     *   - 默认值是 "(inferred)"，会自动推断 close 或 shutdown 方法
     *   - 设置为 "" 可以禁用自动推断
     *
     * @return LifecycleBean 实例
     */
    @Bean(initMethod = "customInit", destroyMethod = "customDestroy")
    public LifecycleBean lifecycleBean() {
        LifecycleBean bean = new LifecycleBean();
        // 这里可以设置属性
        bean.setProperty("测试属性值");
        return bean;
    }
}

