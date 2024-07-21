package com.atguigu.boot;

import com.atguigu.boot.bean.Person;
import com.atguigu.boot.bean.Student;
import com.atguigu.boot.bean.Teacher;
import com.atguigu.boot.bean2.AClass1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

/**
 * @author lfy
 * @Description 启动SpringBoot项目的主入口程序
 * @create 2023-03-27 18:25
 */
// 主程序：com.atguigu.boot
//@SpringBootApplication(scanBasePackages = "com.atguigu")
//@SpringBootConfiguration
//@EnableAutoConfiguration
//@ComponentScan("com.atguigu") 可以使用这个注解指定包扫描路
@Slf4j
@SpringBootApplication // 这是一个SpringBoot应用
public class MainApplication {

    // 测试bean2包下的
    public static void test01(ApplicationContext ioc) {
        // 1、获取容器中所有组件的名字
        String[] beanNames = ioc.getBeanDefinitionNames();
        // 2、挨个遍历：
        // dispatcherServlet、beanNameViewResolver、characterEncodingFilter、multipartResolver
        // SpringBoot把以前配置的核心组件现在都给我们自动配置好了。
        /*
            dispatcherServlet
            beanNameViewResolver
            characterEncodingFilter
            multipartResolver
         */
        Arrays.stream(beanNames).forEach(System.out::println);
        Person person = ioc.getBean(Person.class);
        log.info("person={}", person);

        log.warn("===== 用|表示大文本,会保留格式");
        String text2 = person.getChild()
                .getText()
                .get(2);
        log.info("get Child 2 is {}", text2);

        log.info("==== 用>表示大文本，会压缩换行变成 空格");
        String text3 = person.getChild()
                .getText()
                .get(3);
        log.info("get Child 3 is {}", text3);
        log.info("==== 用|表示大文本，会压缩换行变成 空格");
        var text4 = person.getChild()
                .getText()
                .get(4);
        log.info("get Child 4 is {}", text4);
        log.info("get bean Name for type ={}", Arrays.toString(ioc.getBeanNamesForType(Teacher.class)));
        log.info("get bean teacher is {}", ioc.getBean(Teacher.class));
        log.info("--------------------------------");
        log.info("get bean Student is {}", ioc.getBean(Student.class));

    }

    public static void test02(ApplicationContext ioc) {
        AClass1 a = ioc.getBean(AClass1.class);
        System.out.println(a);

        Student bean = ioc.getBean(Student.class);
        System.out.println(bean);
    }

    public static void main(String[] args) {
        // java10： 局部变量类型的自动推断
        var ioc = SpringApplication.run(MainApplication.class, args);
//        test01(ioc);
        test02(ioc);
    }
}
