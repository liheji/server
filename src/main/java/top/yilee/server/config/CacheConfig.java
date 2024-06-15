package top.yilee.server.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * @author : Galaxy
 * @time : 2023/2/13 23:01
 * @create : IdeaJ
 * @project : server
 * @description :
 */
@EnableCaching
@Configuration
public class CacheConfig {
    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> method.getName() + Arrays.asList(params);
    }
}
