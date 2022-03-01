package top.liheji.server;

import lombok.Cleanup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@SpringBootTest
class ServerApplicationTests {

    @Autowired
    JedisPool jedisPool;

    @Test
    void contextLoads() {
        @Cleanup Jedis jedis = jedisPool.getResource();
        System.out.println(jedis.get("nihao"));;
    }
}
