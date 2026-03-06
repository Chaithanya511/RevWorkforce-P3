package com.rev.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter implements GlobalFilter {

    private AtomicInteger requestCount = new AtomicInteger(0);
    private static final int LIMIT = 200;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        if (requestCount.incrementAndGet() > LIMIT) {
            return Mono.error(new RuntimeException("Rate limit exceeded"));
        }

        return chain.filter(exchange);
    }
}