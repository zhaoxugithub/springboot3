package com.atguigu.boot.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Spring Bean 生命周期演示类
 * 演示 Bean 从创建到销毁的完整生命周期
 *
 * 生命周期顺序：
 * 1. 构造器
 * 2. 设置属性值
 * 3. BeanNameAware.setBeanName()
 * 4. BeanFactoryAware.setBeanFactory()
 * 5. ApplicationContextAware.setApplicationContext()
 * 6. BeanPostProcessor.postProcessBeforeInitialization()
 * 7. @PostConstruct 注解的方法
 * 8. InitializingBean.afterPropertiesSet()
 * 9. 自定义的 init-method
 * 10. BeanPostProcessor.postProcessAfterInitialization()
 * --- Bean 可以使用了 ---
 * 11. @PreDestroy 注解的方法
 * 12. DisposableBean.destroy()
 * 13. 自定义的 destroy-method
 */
public class LifecycleBean implements BeanNameAware, BeanFactoryAware,
        ApplicationContextAware, InitializingBean, DisposableBean {

    private String beanName;
    private BeanFactory beanFactory;
    private ApplicationContext applicationContext;
    private String property;

    /**
     * 第1步：构造器
     * Spring 容器通过反射调用无参构造器创建 Bean 实例
     */
    public LifecycleBean() {
        System.out.println("1. 【构造器】LifecycleBean 构造方法执行");
    }

    /**
     * 第2步：属性注入
     * Spring 通过 setter 方法注入属性值
     */
    public void setProperty(String property) {
        this.property = property;
        System.out.println("2. 【属性注入】setProperty 方法执行，property = " + property);
    }

    /**
     * 第3步：BeanNameAware 接口方法
     * 注入 Bean 的名称，让 Bean 知道自己在容器中的名字
     */
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        System.out.println("3. 【BeanNameAware】setBeanName 方法执行，beanName = " + name);
    }

    /**
     * 第4步：BeanFactoryAware 接口方法
     * 注入 BeanFactory 容器，让 Bean 能够访问容器
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        System.out.println("4. 【BeanFactoryAware】setBeanFactory 方法执行");
    }

    /**
     * 第5步：ApplicationContextAware 接口方法
     * 注入 ApplicationContext 容器，功能比 BeanFactory 更强大
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        System.out.println("5. 【ApplicationContextAware】setApplicationContext 方法执行");
    }

    /**
     * 第7步：@PostConstruct 注解方法
     * JSR-250 规范定义的注解，在依赖注入完成后执行
     * 常用于初始化资源，如线程池、连接池等
     */
    @PostConstruct
    public void postConstruct() {
        System.out.println("7. 【@PostConstruct】postConstruct 方法执行");
    }

    /**
     * 第8步：InitializingBean 接口方法
     * 在所有属性设置完成后执行，可以进行自定义初始化逻辑
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("8. 【InitializingBean】afterPropertiesSet 方法执行");
    }

    /**
     * 第9步：自定义初始化方法
     * 通过 @Bean(initMethod = "customInit") 或 XML 配置 init-method 指定
     * 这是最推荐的初始化方式，解耦且灵活
     */
    public void customInit() {
        System.out.println("9. 【init-method】customInit 自定义初始化方法执行");
    }

    /**
     * Bean 的业务方法
     * 此时 Bean 已经完全初始化，可以正常使用
     */
    public void doSomething() {
        System.out.println(">>> Bean 正常工作中，执行业务逻辑...");
        System.out.println(">>> 当前 Bean 名称: " + beanName);
        System.out.println(">>> 当前属性值: " + property);
    }

    /**
     * 第11步：@PreDestroy 注解方法
     * JSR-250 规范定义的注解，在 Bean 销毁前执行
     * 常用于释放资源，如关闭连接、停止线程等
     */
    @PreDestroy
    public void preDestroy() {
        System.out.println("11. 【@PreDestroy】preDestroy 方法执行");
    }

    /**
     * 第12步：DisposableBean 接口方法
     * Bean 销毁时执行，用于清理资源
     */
    @Override
    public void destroy() throws Exception {
        System.out.println("12. 【DisposableBean】destroy 方法执行");
    }

    /**
     * 第13步：自定义销毁方法
     * 通过 @Bean(destroyMethod = "customDestroy") 或 XML 配置 destroy-method 指定
     * 这是最推荐的销毁方式，解耦且灵活
     */
    public void customDestroy() {
        System.out.println("13. 【destroy-method】customDestroy 自定义销毁方法执行");
    }
}

