package com.atguigu.boot3.rpc.config;

import com.atguigu.boot3.rpc.service.ExpressApi;
import com.atguigu.boot3.rpc.service.WeatherInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * @author lfy
 * @Description
 * @create 2023-05-07 12:42
 */
@Configuration // 最好起名为 AliyunApiConfiguration
public class WeatherConfiguration {
    @Bean
    HttpServiceProxyFactory httpServiceProxyFactory(@Value("${aliyun.appcode}") String appCode) {
        // 1、创建客户端
        WebClient client = WebClient.builder().defaultHeader("Authorization", "APPCODE " + appCode).codecs(clientCodecConfigurer -> {
            clientCodecConfigurer.defaultCodecs().maxInMemorySize(256 * 1024 * 1024);
            // 响应数据量太大有可能会超出BufferSize，所以这里设置的大一点
        }).build();
        // 2、创建工厂
        return HttpServiceProxyFactory.builder(WebClientAdapter.forClient(client)).build();
    }

    @Bean
    WeatherInterface weatherInterface(HttpServiceProxyFactory httpServiceProxyFactory) {
        // 3、获取代理对象
        return httpServiceProxyFactory.createClient(WeatherInterface.class);
    }

    @Bean
    ExpressApi expressApi(HttpServiceProxyFactory httpServiceProxyFactory) {
        return httpServiceProxyFactory.createClient(ExpressApi.class);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }
}
