package com.serendipity.controller;

import com.serendipity.mongo.User;
import com.serendipity.service.UserRepository;
import com.serendipity.util.CheckUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserRepository userRepository;

    /**
     * 构造函数的方式注入（官方推荐，降低耦合）
     */
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public Flux<User> getAll() {
        return userRepository.findAll();
    }

    /**
     * 推荐当返回Flux时新增另一个相同的方法通过流的方式获取数据
     * 以SSE形式多次放回数据
     *
     * @return
     */
    @GetMapping(value = "/stream/all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<User> streamGetAll() {
        return userRepository.findAll();
    }

    /**
     * 新增数据
     *
     * @param user
     * @return
     */
    @PostMapping("/")
    public Mono<User> createUser(@Valid @RequestBody User user) {
        // 校验name字段
        CheckUtil.checkName(user.getName());
        // spring data jpa里面，新增和修改都是save，有id是修改，id为空时新增
        // 根据实际情况是否置空id
        user.setId(null);
        return this.userRepository.save(user);
    }

    /**
     * 根据id删除用户
     * 存在的时候返回200，不存在返回404
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable("id") String id) {

        // deleteById 没有返回值，不能判断数据是否存在
        // this.userRepository.deleteById(id);
        return this.userRepository.findById(id)
                                  // 当你要操作数据，并返回一个Mono，这个时候使用flatMap
                                  // 如果不操作数据，只是转换数据，使用map
                                  .flatMap(user -> this.userRepository.delete(user)
                                                                      .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))

                                  // 如果user数据不存在
                                  .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }

    /**
     * 修改数据
     * 存在的时候返回200和修改后的数据，不存在的时候返回404
     *
     * @param id
     * @param user
     * @return
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<User>> updateUser(@PathVariable("id") String id, @Valid @RequestBody User user) {
        // 校验name字段
        CheckUtil.checkName(user.getName());
        return this.userRepository.findById(id)
                                  // flatMap: 操作数据
                                  .flatMap(u -> {
                                      u.setAge(user.getAge());
                                      u.setName(user.getName());
                                      return this.userRepository.save(u);
                                  })
                                  // map: 转换数据
                                  .map(u -> new ResponseEntity<>(u, HttpStatus.OK))
                                  .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    /**
     * 根据ID查找用户
     * 存在返回用户信息，不存在返回404
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<User>> findUser(@PathVariable("id") String id) {
        return this.userRepository.findById(id)
                                  .map(u -> new ResponseEntity<>(u, HttpStatus.OK))
                                  .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 根据年龄查找用户
     *
     * @param start
     * @param end
     * @return
     */
    @GetMapping("/age/{start}/{end}")
    public Flux<User> findByAge(@PathVariable("start") int start, @PathVariable("end") int end) {
        return this.userRepository.findByAgeBetween(start, end);
    }

    /**
     * 根据年龄查找用户,以SSE形式多次放回数据
     *
     * @param start
     * @param end
     * @return
     */
    @GetMapping(value = "/stream/age/{start}/{end}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<User> streamFindByAge(@PathVariable("start") int start, @PathVariable("end") int end) {
        return this.userRepository.findByAgeBetween(start, end);
    }

    /**
     * 得到20-30岁用户,以SSE形式多次放回数据
     *
     * @return
     */
    @GetMapping(value = "/stream/old", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<User> streamOldUser() {
        return this.userRepository.oldUser();
    }

    /**
     * 得到20-30岁用户
     *
     * @return
     */
    @GetMapping("/old")
    public Flux<User> oldUser() {
        return this.userRepository.oldUser();
    }
}
