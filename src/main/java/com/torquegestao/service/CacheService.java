package com.torquegestao.service;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis: cache do dashboard e rate limit do login.
 *
 * <p>Tudo é "fail-open": se o Redis estiver fora do ar, a aplicação continua funcionando
 * (sem cache e sem rate limit) em vez de derrubar as requisições.
 */
@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private final StringRedisTemplate redis;

    public CacheService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public String get(String key) {
        try {
            return redis.opsForValue().get(key);
        } catch (RuntimeException ex) {
            log.warn("Redis indisponível (get {}): {}", key, ex.getMessage());
            return null;
        }
    }

    public void set(String key, String value, int ttlSeconds) {
        try {
            redis.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
        } catch (RuntimeException ex) {
            log.warn("Redis indisponível (set {}): {}", key, ex.getMessage());
        }
    }

    public void delete(String key) {
        try {
            redis.delete(key);
        } catch (RuntimeException ex) {
            log.warn("Redis indisponível (delete {}): {}", key, ex.getMessage());
        }
    }

    /** Conta uma tentativa e devolve {@code true} se o limite da janela já foi excedido. */
    public boolean hitRateLimit(String key, int limit, int windowSeconds) {
        try {
            Long count = redis.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redis.expire(key, Duration.ofSeconds(windowSeconds));
            }
            return count != null && count > limit;
        } catch (RuntimeException ex) {
            log.warn("Redis indisponível (rate limit): {}", ex.getMessage());
            return false;
        }
    }

    public boolean ping() {
        try {
            var factory = redis.getConnectionFactory();
            if (factory == null) {
                return false;
            }
            try (var connection = factory.getConnection()) {
                return "PONG".equalsIgnoreCase(connection.ping());
            }
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
