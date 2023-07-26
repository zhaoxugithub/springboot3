package com.serendipity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;


@Configuration
public class AllRouters {
    @Bean
    RouterFunction<ServerResponse> userRouter(UserHandler userHandler) {
        return RouterFunctions.nest(
                // 相当于类上面的@RequestMapping("/user")
                RequestPredicates.path("/user"),
                RouterFunctions
                        // 相当于类里面的@GetMapping("/")
                        // 得到所有用户
                        .route(RequestPredicates.GET("/"),
                                userHandler::getAllUser)
                        // 创建用户
                        .andRoute(RequestPredicates.POST("/").
                                        and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                                userHandler::createUser)
                        // 删除用户
                        .andRoute(RequestPredicates.DELETE("/{id}"),
                                userHandler::deleteUserById)
        );
    }
}
