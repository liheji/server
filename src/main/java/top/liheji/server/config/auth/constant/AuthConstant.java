package top.liheji.server.config.auth.constant;

import cn.hutool.core.util.IdUtil;

/**
 * @author : Galaxy
 * @time : 2023/2/12 20:22
 * @create : IdeaJ
 * @project : server
 * @description :
 */
public class AuthConstant {
    /**
     * 记住密码失效时间
     */
    public static final long REMEMBER_EXPIRE_SECONDS = 14 * 24 * 60 * 60;

    public static final String REMEMBER_COOKIE_NAME = "sessionid";

    public static final String REMEMBER_PARAMETER = "remember";

    /**
     * 记住密码 KEY
     */
    public static final String REMEMBER_ME_KEY = IdUtil.randomUUID();
}
