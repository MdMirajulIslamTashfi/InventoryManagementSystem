package com.tashfi.InventoryManagementSystem.core.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@Order(1)
public class RequestLoggingFilter implements WebFilter {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String uri = request.getURI().toString();

        MDC.put("Method", method);
        MDC.put("Uri", path);

        String receivedAt = LocalDateTime.now().format(FORMATTER);

        log.info("""
                        Request Received From {}
                        Uri     : {}
                        Method  : {}
                        Headers : {}
                        Path    : {}
                        Params  : {}
                        """,
                request.getRemoteAddress(),
                uri,
                method,
                request.getHeaders(),
                path,
                request.getQueryParams()
        );

        return chain.filter(exchange)
                .doFinally(signal -> {
                    int status = response.getStatusCode() != null
                            ? response.getStatusCode().value() : 0;
                    String sentAt = LocalDateTime.now().format(FORMATTER);

                    if (status >= 400) {
                        log.error("""
                                        Response Sending To {}
                                        Uri          : {}
                                        Path         : {}
                                        Headers      : {}
                                        Status       : {}
                                        Received At  : {}
                                        Sent At      : {}
                                        """,
                                request.getRemoteAddress(),
                                uri, path,
                                response.getHeaders(),
                                response.getStatusCode(),
                                receivedAt, sentAt
                        );
                    } else {
                        log.info("""
                                        Response Sending To {}
                                        Uri          : {}
                                        Path         : {}
                                        Headers      : {}
                                        Status       : {}
                                        Received At  : {}
                                        Sent At      : {}
                                        """,
                                request.getRemoteAddress(),
                                uri, path,
                                response.getHeaders(),
                                response.getStatusCode(),
                                receivedAt, sentAt
                        );
                    }

                    MDC.remove("Method");
                    MDC.remove("Uri");
                });
    }
}