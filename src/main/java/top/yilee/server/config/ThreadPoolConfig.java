package top.yilee.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @author : Galaxy
 * @time : 2023/1/25 22:13
 * @create : IdeaJ
 * @project : gulimall
 * @description :
 */
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ScheduledThreadPoolExecutor threadPoolExecutor() {
        return new ScheduledThreadPoolExecutor(
                10,
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
