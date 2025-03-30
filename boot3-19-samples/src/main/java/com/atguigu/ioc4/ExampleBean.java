package com.atguigu.ioc4;

import org.apache.naming.factory.BeanFactory;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Service;

@Service
public class ExampleBean {
    public ExampleBean() {
        System.out.println("1. 实例化构造函数");
    }

    public void setBeanName(String name) {
        System.out.println("3. BeanNameAware.setBeanName: " + name);
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("4. BeanFactoryAware.setBeanFactory");
    }

    public void afterPropertiesSet() {
        System.out.println("7. InitializingBean.afterPropertiesSet");
    }

    public void customInit() {
        System.out.println("8. 自定义初始化方法");
    }

    public void sayHello() {
        System.out.println("9. 使用Bean的业务方法");
    }

    public void destroy() {
        System.out.println("11. DisposableBean.destroy");
    }

    public void customDestroy() {
        System.out.println("12. 自定义销毁方法");
    }
}
