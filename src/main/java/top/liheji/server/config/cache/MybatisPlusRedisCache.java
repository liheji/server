package top.liheji.server.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisTemplate;
import top.liheji.server.util.CypherUtils;
import top.liheji.server.util.BeanUtils;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author : Galaxy
 * @time : 2022/3/2 10:23
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 使用 redis实现 MybatisPlus二级缓存
 */
@Slf4j
public class MybatisPlusRedisCache implements Cache {

    private RedisTemplate<String, Object> redisTemplate;
    /**
     * cache ID
     */
    private final String id;

    /**
     * 读写锁
     */
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);


    public MybatisPlusRedisCache(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void putObject(Object key, Object value) {
        checkTemplate();
        try {
            //将key加密后存入
            redisTemplate.opsForHash().put(this.id, CypherUtils.encodeToHash(key.toString(), "SHA-256"), value);
        } catch (Exception e) {
            log.error(this.id + " 保存数据到Redis缓存错误，信息：" + e);
        }
    }

    @Override
    public Object getObject(Object key) {
        checkTemplate();
        try {
            if (key != null) {
                return redisTemplate.opsForHash().get(this.id, CypherUtils.encodeToHash(key.toString(), "SHA-256"));
            }
        } catch (Exception e) {
            log.error(this.id + " 获取Redis缓存数据错误，信息：" + e);
        }
        return null;
    }

    @Override
    public Object removeObject(Object key) {
        checkTemplate();
        try {
            if (key != null) {
                redisTemplate.opsForHash().delete(this.id, CypherUtils.encodeToHash(key.toString(), "SHA-256"));
            }
        } catch (Exception e) {
            log.error(this.id + " 移除Redis缓存数据错误，信息：" + e);
        }

        return null;
    }

    @Override
    public void clear() {
        checkTemplate();
        try {
            redisTemplate.delete(this.id);
        } catch (Exception e) {
            log.error(this.id + " 清空Redis缓存数据错误，信息：" + e);
        }

    }

    @Override
    public int getSize() {
        checkTemplate();
        Long size = redisTemplate.execute(RedisServerCommands::dbSize);
        if (size == null) {
            size = 0L;
        }
        return size.intValue();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return this.readWriteLock;
    }

    private void checkTemplate() {
        if (this.redisTemplate == null) {
            //由于启动期间注入失败，只能运行期间注入，这段代码可以删除
            this.redisTemplate = (RedisTemplate<String, Object>) BeanUtils.getBean("redisTemplate");
        }
    }
}


