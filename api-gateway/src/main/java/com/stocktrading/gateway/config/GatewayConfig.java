package com.stocktrading.gateway.config;

import com.stocktrading.gateway.filter.AuthenticationFilter;
import com.stocktrading.gateway.filter.RateLimitingFilter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;

    public GatewayConfig(AuthenticationFilter authenticationFilter, RateLimitingFilter rateLimitingFilter) {
        this.authenticationFilter = authenticationFilter;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth routes - no authentication required
                .route("auth-signup", r -> r
                        .path("/api/auth/signup")
                        .uri("http://localhost:8081"))
                .route("auth-login", r -> r
                        .path("/api/auth/login")
                        .uri("http://localhost:8081"))
                .route("auth-validate", r -> r
                        .path("/api/auth/validate")
                        .uri("http://localhost:8081"))

                // User routes
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config() {
                            {
                                setRequired(true);
                            }
                        })))
                        .uri("http://localhost:8081"))

                // Trading routes
                .route("trading-service", r -> r
                        .path("/api/orders/**", "/api/trades/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config() {
                            {
                                setRequired(true);
                            }
                        }))
                                .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config() {
                                    {
                                        setName("trading-limiter");
                                    }
                                })))
                        .uri("http://localhost:8082"))

                // Portfolio routes
                .route("portfolio-service", r -> r
                        .path("/api/portfolio/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config() {
                            {
                                setRequired(true);
                            }
                        })))
                        .uri("http://localhost:8083"))

                // Market data routes - public
                .route("market-data-service", r -> r
                        .path("/api/market/**")
                        .uri("http://localhost:8084"))

                // Notification routes
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config() {
                            {
                                setRequired(true);
                            }
                        })))
                        .uri("http://localhost:8085"))

                // Payment routes
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config() {
                            {
                                setRequired(true);
                            }
                        }))
                                .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config() {
                                    {
                                        setName("payment-limiter");
                                    }
                                })))
                        .uri("http://localhost:8086"))

                // Analytics routes
                .route("analytics-service", r -> r
                        .path("/api/analytics/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config() {
                            {
                                setRequired(true);
                            }
                        })))
                        .uri("http://localhost:8087"))
                .build();
    }

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig defaultConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .limitForPeriod(100)
                .timeoutDuration(Duration.ofSeconds(5))
                .build();

        RateLimiterConfig tradingConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .limitForPeriod(500)
                .timeoutDuration(Duration.ofSeconds(5))
                .build();

        RateLimiterConfig paymentConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .limitForPeriod(50)
                .timeoutDuration(Duration.ofSeconds(5))
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(defaultConfig);
        registry.rateLimiter("trading-limiter", tradingConfig);
        registry.rateLimiter("payment-limiter", paymentConfig);

        return registry;
    }
}
