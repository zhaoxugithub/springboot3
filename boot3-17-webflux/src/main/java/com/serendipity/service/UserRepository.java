package com.serendipity.service;

import com.serendipity.mongo.User;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    /**
     * 根据年龄查找用户
     *
     * @param start
     * @param end
     * @return
     */
    Flux<User> findByAgeBetween(int start, int end);

    /**
     * 使用查询语句查询20-30岁的用户
     *
     * @return
     */
    @Query("{'age':{'$gte':20, '$lte':30}}")
    Flux<User> oldUser();
}
