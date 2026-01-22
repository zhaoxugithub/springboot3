package com.atguigu.boot.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Bean 后置处理器（BeanPostProcessor）
 *
 * 作用：对容器中所有 Bean 的初始化进行前置和后置处理
 * 这是 Spring 框架的重要扩展点，AOP、事务等功能都基于此实现
 *
 * 执行时机：
 * - postProcessBeforeInitialization: 在 Bean 初始化方法（@PostConstruct、InitializingBean、init-method）之前执行
 * - postProcessAfterInitialization: 在 Bean 初始化方法之后执行，可以返回代理对象
 *
 * 应用场景：
 * 1. 创建代理对象（AOP）
 * 2. 属性验证
 * 3. 日志记录
 * 4. 性能监控
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

    /**
     * 第6步：初始化前的后置处理
     *
     * 在以下方法之前执行：
     * - @PostConstruct
     * - InitializingBean.afterPropertiesSet()
     * - init-method
     *
     * @param bean 当前正在初始化的 Bean 实例
     * @param beanName Bean 的名称
     * @return 返回的对象会替换原始 Bean（可以返回代理对象）
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof LifecycleBean) {
            System.out.println("6. 【BeanPostProcessor】postProcessBeforeInitialization 执行，beanName = " + beanName);
        }
        // 这里可以对 Bean 进行增强，比如属性校验、日志记录等
        return bean;
    }

    /**
     * 第10步：初始化后的后置处理
     *
     * 在以下方法之后执行：
     * - @PostConstruct
     * - InitializingBean.afterPropertiesSet()
     * - init-method
     *
     * 这是创建代理对象的关键时机！
     * Spring AOP 就是在这里返回代理对象的
     *
     * @param bean 已经初始化完成的 Bean 实例
     * @param beanName Bean 的名称
     * @return 返回的对象会替换原始 Bean（通常返回代理对象）
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof LifecycleBean) {
            System.out.println("10. 【BeanPostProcessor】postProcessAfterInitialization 执行，beanName = " + beanName);
        }
        // 这里可以返回代理对象，实现 AOP、事务等功能
        // 例如：return Proxy.newProxyInstance(...)
        return bean;
    }
}

