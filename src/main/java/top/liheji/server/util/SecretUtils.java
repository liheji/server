package top.liheji.server.util;

import lombok.extern.slf4j.Slf4j;
import top.liheji.server.pojo.Account;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Time : 2022/1/25 10:19
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : mybatis-gen
 * @Description :
 */
@Slf4j
public class SecretUtils {
    private static final Random RANDOM = new Random();
    private static final String[] CHARS = "0,1,2,3,4,5,6,7,9,8".split(",");
    private static final Map<String, Object[]> SECRET_CACHE = new ConcurrentHashMap<>();

    /**
     * 生成秘钥
     *
     * @param type 生成类型 false: WebSocket 5分钟 true: 通用 Token 3天
     * @return key
     */
    public static String genSecret(Account account, boolean type) {
        //删除冗余的秘钥
        for (Map.Entry<String, Object[]> it : SECRET_CACHE.entrySet()) {
            if ((Long) it.getValue()[1] <= System.currentTimeMillis()) {
                SECRET_CACHE.remove(it.getKey());
            }
        }

        long timeMillis = type ? 3 * 24 * 60 * 60 * 1000 : 10 * 60 * 1000;

        //设置秘钥
        String key = StringUtils.genUuidWithoutLine();
        log.info(String.format("%s 用户创建了授权码: %s", account.getUsername(), key));
        SECRET_CACHE.put(key,
                new Object[]{
                        account.getUsername(),
                        System.currentTimeMillis() + timeMillis
                });

        return key;
    }

    /**
     * 生成验证码
     *
     * @param len 生成验证码的长度
     * @return key
     */
    public static String genCaptcha(Account account, Integer len) {
        //删除冗余的秘钥
        for (Map.Entry<String, Object[]> it : SECRET_CACHE.entrySet()) {
            if ((Long) it.getValue()[1] <= System.currentTimeMillis()) {
                SECRET_CACHE.remove(it.getKey());
            }
        }

        StringBuilder builder = new StringBuilder();
        while (builder.length() <= 0 || SECRET_CACHE.containsKey(builder.toString())) {
            builder.setLength(0);
            for (int i = 0; i < len; i++) {
                builder.append(CHARS[RANDOM.nextInt(CHARS.length)]);
            }
        }

        //设置秘钥
        log.info(String.format("%s 用户创建了验证码: %s", account.getUsername(), builder));
        SECRET_CACHE.put(builder.toString(),
                new Object[]{
                        account.getUsername(),
                        System.currentTimeMillis() + 10 * 60 * 1000
                });

        return builder.toString();
    }

    /**
     * 验证秘钥
     *
     * @param key 验证秘钥
     * @return 是否成功
     */
    public static boolean check(String key) {
        if (key != null && SECRET_CACHE.containsKey(key)) {
            boolean tmp = (Long) SECRET_CACHE.get(key)[1] > System.currentTimeMillis();
            SECRET_CACHE.remove(key);
            return tmp;
        }
        return false;
    }
}
