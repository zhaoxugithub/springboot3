package com.serendipity.exception;

import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
// 设置优先级，最少要调到-2，否则不会工作，数值越小优先级越高
@Order(-2)
public class ExceptionHandler implements WebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        // 设置响应头400
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        // 设置返回类型
        response.getHeaders()
                .setContentType(MediaType.TEXT_PLAIN);
        // 异常信息
        String errorMsg = toStr(ex);
        DataBuffer db = response.bufferFactory()
                .wrap(errorMsg.getBytes());
        return response.writeWith(Mono.just(db));
    }

    private String toStr(Throwable ex) {
        // 已知异常
        if (ex instanceof CheckException) {
            CheckException e = (CheckException) ex;
            return e.getFieldName() + ":不合法的值->" + e.getFieldValue();
        } else {
            // 未知异常，需要打印堆栈，方便定位
            ex.printStackTrace();
            return ex.toString();
        }
    }
}
