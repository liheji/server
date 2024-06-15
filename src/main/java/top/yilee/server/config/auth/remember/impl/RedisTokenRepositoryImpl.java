package top.yilee.server.config.auth.remember.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import top.yilee.server.config.auth.constant.AuthConstant;
import top.yilee.server.constant.ServerConstant;
import top.yilee.server.pojo.AuthDevices;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author : Galaxy
 * @time : 2023/2/12 16:21
 * @create : IdeaJ
 * @project : server
 * @description :
 */
@Component("redisTokenRepository")
public class RedisTokenRepositoryImpl implements PersistentTokenRepository {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 令牌前缀
     */
    private final static String REDIS_TOKEN_PREFIX = "server:rememberme:";

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        // 删除原有的token
        removeUserTokens(token.getUsername());
        //生成一个存储 Token信息的Key
        String key = redisKey(token.getSeries());
        //生成一个存储series的key,因为下面removeToken传入的参数为username,所以用username生成一个key来获取唯一的series
        String usernameKey = redisKey(token.getUsername());

        //存储 usernameKey
        stringRedisTemplate.opsForValue().set(usernameKey, token.getSeries(), AuthConstant.REMEMBER_EXPIRE_SECONDS, TimeUnit.SECONDS);

        //将Token数据存入redis
        stringRedisTemplate.opsForHash().putAll(key, toMap(token));
        stringRedisTemplate.expire(key, AuthConstant.REMEMBER_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        String key = redisKey(series);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            stringRedisTemplate.opsForHash().put(key, "tokenValue", tokenValue);
            stringRedisTemplate.opsForHash().put(key, "date", String.valueOf(lastUsed.getTime()));
        }
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        String key = redisKey(seriesId);
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        if (ObjectUtils.isEmpty(entries)) {
            return null;
        }
        return toToken(entries);
    }

    @Override
    public void removeUserTokens(String username) {
        String key = redisKey(username);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            String series = stringRedisTemplate.opsForValue().get(key);
            stringRedisTemplate.delete(redisKey(series));
            stringRedisTemplate.delete(key);
        }
    }

    /**
     * 删除指定设备的登录信息
     *
     * @param username 用户名
     * @param device   设备信息
     */
    public void removeUserTokens(String username, String device) {
        String userKey = REDIS_TOKEN_PREFIX + device + ":" + username;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(userKey))) {
            String series = stringRedisTemplate.opsForValue().get(userKey);
            String seriesKey = REDIS_TOKEN_PREFIX + device + ":" + series;
            stringRedisTemplate.delete(seriesKey);
            stringRedisTemplate.delete(userKey);
        }
    }

    /**
     * 根据用户名获取Series
     *
     * @param username 用户名
     * @return Series
     */
    public String getSeriesForUsername(String username) {
        String key = redisKey(username);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            return stringRedisTemplate.opsForValue().get(key);
        }
        return null;
    }

    /**
     * 拼接存储 key
     *
     * @param key key
     * @return
     */
    private static String redisKey(String key) {
        AuthDevices device = ServerConstant.LOCAL_DEVICE.get();
        return REDIS_TOKEN_PREFIX + device.getType() + ":" + key;
    }

    /**
     * 将 PersistentRememberMeToken 转化为Map
     *
     * @param token 实体类
     * @return
     */
    private static Map<String, String> toMap(PersistentRememberMeToken token) {
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("username", token.getUsername());
        tokenMap.put("series", token.getSeries());
        tokenMap.put("tokenValue", token.getTokenValue());
        tokenMap.put("date", String.valueOf(token.getDate().getTime()));
        return tokenMap;
    }

    /**
     * 将 map转化为 PersistentRememberMeToken
     *
     * @param tokenMap Map
     * @return
     */
    private static PersistentRememberMeToken toToken(Map<Object, Object> tokenMap) {
        PersistentRememberMeToken token = new PersistentRememberMeToken(
                String.valueOf(tokenMap.get("username")),
                String.valueOf(tokenMap.get("series")),
                String.valueOf(tokenMap.get("tokenValue")),
                new Date(Long.parseLong(String.valueOf(tokenMap.get("date"))))
        );

        if (ObjectUtils.isEmpty(token.getUsername()) ||
                ObjectUtils.isEmpty(token.getSeries()) ||
                ObjectUtils.isEmpty(token.getTokenValue()) ||
                ObjectUtils.isEmpty(token.getDate())) {
            return null;
        }

        return token;
    }
}
