package top.liheji.server.config;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import top.liheji.server.config.properties.JedisPoolProperties;
import top.liheji.server.config.properties.JedisProperties;

/**
 * @Time : 2021/11/25 23:46
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : server
 * @Description :
 */
@Slf4j
@Configuration
public class JedisConfig {
    @Autowired
    private JedisProperties jedisProperties;

    @Autowired
    private JedisPoolProperties jedisPoolProperties;

    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(jedisPoolProperties.getMaxActive());
        jedisPoolConfig.setMaxIdle(jedisPoolProperties.getMaxIdle());
        jedisPoolConfig.setMinIdle(jedisPoolProperties.getMinIdle());
        jedisPoolConfig.setMaxWaitMillis(jedisPoolProperties.getMaxWaitMillis());
        jedisPoolConfig.setTestOnBorrow(jedisPoolProperties.isTestOnBorrow());

        if (StringUtils.isEmpty(jedisProperties.getPassword())) {
            jedisProperties.setPassword(null);
        }

        JedisPool pool = new JedisPool(
                jedisPoolConfig,
                jedisProperties.getHost(),
                jedisProperties.getPort(),
                jedisProperties.getTimeout(),
                jedisProperties.getPassword(),
                jedisProperties.getDatabase(),
                jedisProperties.getClientName(),
                jedisProperties.isSsl()
        );

        log.info("JedisPool注入成功");

        return pool;
    }
}
