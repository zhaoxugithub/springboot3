package com.atguigu.aop2;

import jakarta.annotation.Resource;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;


@Aspect
@Component
public class CacheAspect {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 定义切入点：所有被@RedisCache注解的方法
    @Around("@annotation(redisCache)")
    public Object cache(ProceedingJoinPoint joinPoint, RedisCache redisCache) throws Throwable {
        String key = generateKey(redisCache.key(), joinPoint);
        Object value = redisTemplate.opsForValue().get(key);

        if (value != null) {
            return value; // 缓存命中直接返回
        }

        // 缓存未命中，执行原方法
        Object result = joinPoint.proceed();
        if (result != null) {
            redisTemplate.opsForValue().set(key, result, redisCache.expire(), TimeUnit.SECONDS);
        }
        return result;
    }

    // 定义切入点：所有被@CacheEvict注解的方法
    @Around("@annotation(cacheEvict)")
    public Object evictCache(ProceedingJoinPoint joinPoint, CacheEvict cacheEvict) throws Throwable {
        String key = generateKey(cacheEvict.key(), joinPoint);
        redisTemplate.delete(key);
        return joinPoint.proceed();
    }

    // 生成缓存key（支持SpEL表达式）
    private String generateKey(String keyTemplate, ProceedingJoinPoint joinPoint) {
        // 获取方法参数名和值
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 创建 SpEL 解析上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
        String[] parameterNames = nameDiscoverer.getParameterNames(method);

        // 将参数名与参数值绑定到上下文
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }

        // 解析表达式（如 "#id" 会被替换为参数值）
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(keyTemplate);
        return expression.getValue(context, String.class);
    }

}
