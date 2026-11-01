package com.interviewiq.gateway.filter;

import com.interviewiq.gateway.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Har request pe chalta hai (routing se pehle).
 *  1. Client ke bheje X-User-* headers hata deta hai (warna koi khud ko admin bata dega).
 *  2. Public paths (register/login) ko bina token jaane deta hai.
 *  3. Baaki sab ke liye "Authorization: Bearer <jwt>" verify karta hai.
 *  4. /admin/ wale paths sirf ADMIN role ke liye.
 *  5. Verified user ki info downstream services ko headers mein bhejta hai:
 *     X-User-Id, X-User-Email, X-User-Role
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATHS = List.of("/auth/register", "/auth/login");

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // CORS preflight browser khud bhejta hai, usme token nahi hota
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String path = request.getURI().getPath();

        // Step 1: spoofed headers hatao
        ServerHttpRequest.Builder builder = request.mutate().headers(h -> {
            h.remove("X-User-Id");
            h.remove("X-User-Email");
            h.remove("X-User-Role");
        });

        // Step 2: public paths
        if (PUBLIC_PATHS.contains(path)) {
            return chain.filter(exchange.mutate().request(builder.build()).build());
        }

        // Step 3: token verify
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return error(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        Claims claims;
        try {
            claims = jwtUtil.parseToken(authHeader.substring(7).trim());
        } catch (JwtException | IllegalArgumentException e) {
            return error(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        String role = claims.get("role", String.class);
        String email = claims.get("email", String.class);

        // Step 4: admin-only paths
        if (path.contains("/admin/") && !"ADMIN".equals(role)) {
            return error(exchange, HttpStatus.FORBIDDEN, "Admin access required");
        }

        // Step 5: downstream ko user info do
        builder.header("X-User-Id", claims.getSubject());
        builder.header("X-User-Email", email == null ? "" : email);
        builder.header("X-User-Role", role == null ? "" : role);

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    @Override
    public int getOrder() {
        return -1;   // routing filters se pehle
    }

    // Error format saari services jaisa: { status, message, timestamp }
    private Mono<Void> error(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String json = "{\"status\":" + status.value()
                + ",\"message\":\"" + message + "\""
                + ",\"timestamp\":\"" + Instant.now() + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
