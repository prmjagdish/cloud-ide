package com.cloud_ide.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class UserIdRelayFilter extends AbstractGatewayFilterFactory<Object> {
    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) ->
                ReactiveSecurityContextHolder.getContext()
                        .flatMap(context -> {
                            var authentication = context.getAuthentication();
                            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                                String userId = jwt.getSubject();
                                var mutatedExchange = exchange.mutate()
                                        .request(r -> r.headers(h -> h.add("userId", userId)))
                                        .build();
                                return chain.filter(mutatedExchange);
                            }
                            return chain.filter(exchange);
                        });
    }
}
