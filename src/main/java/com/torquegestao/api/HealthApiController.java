package com.torquegestao.api;

import com.torquegestao.service.CacheService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Saúde")
@RestController
@RequestMapping("/api/health")
public class HealthApiController {

    private final JdbcTemplate jdbc;
    private final CacheService cache;

    public HealthApiController(JdbcTemplate jdbc, CacheService cache) {
        this.jdbc = jdbc;
        this.cache = cache;
    }

    /** Liveness/readiness: o banco é obrigatório; o Redis é opcional (só degrada). */
    @GetMapping
    public Map<String, String> health() {
        jdbc.queryForObject("SELECT 1", Integer.class);
        return Map.of("status", "ok", "database", "up", "redis", cache.ping() ? "up" : "down");
    }
}
