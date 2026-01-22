package com.atguigu.boot.lifecycle;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Bean 生命周期测试运行器
 *
 * CommandLineRunner 接口：
 * - Spring Boot 提供的接口，在应用启动完成后自动执行
 * - 可以用来执行一些初始化任务、测试代码等
 * - run 方法会在所有 Bean 初始化完成后执行
 *
 * 执行顺序：
 * 1. Spring 容器启动
 * 2. 扫描并创建所有 Bean（触发生命周期）
 * 3. 执行 CommandLineRunner.run()
 * 4. 应用正常运行
 * 5. 应用关闭时触发 Bean 销毁
 */
@Component
public class LifecycleTestRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final LifecycleBean lifecycleBean;

    /**
     * 构造器注入依赖
     * Spring Boot 推荐使用构造器注入，好处：
     * 1. 依赖明确，不可变
     * 2. 便于单元测试
     * 3. 避免循环依赖
     */
    public LifecycleTestRunner(ApplicationContext applicationContext, LifecycleBean lifecycleBean) {
        this.applicationContext = applicationContext;
        this.lifecycleBean = lifecycleBean;
    }

    /**
     * 应用启动后自动执行
     *
     * @param args 命令行参数
     */
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("     Spring Bean 生命周期演示");
        System.out.println("=".repeat(70) + "\n");

        System.out.println(">>> 说明：上面已经完成了 Bean 的创建和初始化过程\n");

        // 调用 Bean 的业务方法
        lifecycleBean.doSomething();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("     准备关闭容器，触发 Bean 销毁流程");
        System.out.println("=".repeat(70) + "\n");

        // 注意：实际的销毁过程会在应用关闭时自动触发
        // 可以通过 Ctrl+C 或正常关闭应用来观察销毁过程
        System.out.println(">>> 提示：请关闭应用（Ctrl+C）以观察 Bean 销毁过程\n");
    }
}

