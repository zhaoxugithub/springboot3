package com.serendipity.config;

import com.serendipity.mongo.User;
import com.serendipity.service.UserRepository;
import com.serendipity.util.CheckUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class UserHandler {
    private final UserRepository userRepository;

    public UserHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 得到所有用户
     *
     * @param request
     * @return
     */
    public Mono<ServerResponse> getAllUser(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(this.userRepository.findAll(), User.class);
    }

    /**
     * 创建用户
     *
     * @param request
     * @return
     */
    public Mono<ServerResponse> createUser(ServerRequest request) {
        Mono<User> user = request.bodyToMono(User.class);
//        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
//                .body(this.userRepository.saveAll(user), User.class);
        return user.flatMap(u -> {
            // 校验代码需要放在这里
            CheckUtil.checkName(u.getName());

            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(this.userRepository.save(u), User.class);
        });
    }

    /**
     * 根据id删除用户
     *
     * @param request
     * @return
     */
    public Mono<ServerResponse> deleteUserById(ServerRequest request) {
        String id = request.pathVariable("id");
        return this.userRepository.findById(id)
                .flatMap(user -> this.userRepository.delete(user).then(ServerResponse.ok().build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
