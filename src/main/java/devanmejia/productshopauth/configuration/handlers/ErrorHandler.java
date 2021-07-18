package devanmejia.productshopauth.configuration.handlers;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;


@ControllerAdvice
public class ErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        ServerHttpResponse response = serverWebExchange.getResponse();
        throwable.printStackTrace();
        DataBuffer dataBuffer = response.bufferFactory().wrap(throwable.getMessage().getBytes(StandardCharsets.UTF_8));
        response.setStatusCode(HttpStatus.NOT_FOUND);
        return response.writeWith(Mono.just(dataBuffer));
    }
}
