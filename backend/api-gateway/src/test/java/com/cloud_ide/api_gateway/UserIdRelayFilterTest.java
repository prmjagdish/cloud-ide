package com.cloud_ide.api_gateway;

import com.cloud_ide.api_gateway.filter.UserIdRelayFilter;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

class UserIdRelayFilterTest {

    private final UserIdRelayFilter filter = new UserIdRelayFilter();

    @Test
    void shouldInjectUserIdHeader() {
        // fake userId
        String userId = UUID.randomUUID().toString();

        // fake JWT with sub = userId
        Jwt jwt = new Jwt(
                "fake-token", Instant.now(), Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", userId, "username", "testuser")
        );

        // fake authentication with JWT
        AbstractAuthenticationToken auth = new AbstractAuthenticationToken(null) {
            @Override
            public Object getCredentials() { return null; }
            @Override
            public Object getPrincipal() { return jwt; }
        };
        auth.setAuthenticated(true);

        // fake exchange (mock request to /projects)
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest.get("/projects").build()
        );

        // mock filter chain
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        when(chain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
            ServerWebExchange mutated = invocation.getArgument(0);

            // ✅ assert header injected
            assert mutated.getRequest().getHeaders().getFirst("userId").equals(userId);
            return Mono.empty();
        });

        // run filter with fake security context
        Mono<Void> result = filter.apply(new Object()).filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));

        // verify completes
        StepVerifier.create(result).verifyComplete();

        // verify chain called once
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }
}