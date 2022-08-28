package top.liheji.server.config.auth;

/**
 * @author : Galaxy
 * @time : 2022/8/26 8:44
 * @create : IdeaJ
 * @project : server
 * @description :
 */
public enum AuthType {
    /**
     * 第三方认证的类型枚举
     */
    QQ("qq", "QQ"),
    Weibo("weibo", "微博", false),
    WeChat("wechat", "微信", false),
    GitHub("github", "GitHub");

    private final String code;
    private final String name;
    private final Boolean enabled;

    AuthType(String code, String name) {
        this.code = code;
        this.name = name;
        this.enabled = true;
    }

    AuthType(String code, String name, Boolean enabled) {
        this.code = code;
        this.name = name;
        this.enabled = enabled;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public static AuthType getByCode(String code) {
        for (AuthType cur : AuthType.values()) {
            if (cur.code.equals(code)) {
                return cur;
            }
        }
        return null;
    }

    public static AuthType getByName(String name) {
        for (AuthType cur : AuthType.values()) {
            if (cur.name.equals(name)) {
                return cur;
            }
        }
        return null;
    }
}
