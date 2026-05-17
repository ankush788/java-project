package com.apigateway.filter.correlation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class CorrelationIdFilter implements GlobalFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        
        String correlationId = UUID.randomUUID().toString();
        String method = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "UNKNOWN";
        String path = exchange.getRequest().getPath().value();
        String remoteAddress = exchange.getRequest().getRemoteAddress() != null ? 
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
                
            log.info("correlationId: {} - [CORRELATION RECEIVED] {} {} - from {}", correlationId, method, path, remoteAddress);
        

        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .headers(headers -> headers.set(CORRELATION_ID_HEADER, correlationId))
                .build();

        exchange.getResponse().beforeCommit(() -> {
            exchange.getResponse().getHeaders().remove(CORRELATION_ID_HEADER);
            exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);
            log.debug("correlationId: {} - [CORRELATION RESPONSE] Setting correlation header in response", correlationId);
            return Mono.empty();
        });

        return chain.filter(exchange.mutate().request(request).build())
                .doFinally(signalType -> {
                    int statusCode = exchange.getResponse().getStatusCode() != null ? 
                            exchange.getResponse().getStatusCode().value() : 0;
                    log.info("correlationId: {} - [REQUEST COMPLETE] {} {} - Status: {}", correlationId, method, path, statusCode);
                });
    }
}
