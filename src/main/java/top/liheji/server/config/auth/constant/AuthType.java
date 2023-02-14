package top.liheji.server.config.auth.constant;

import lombok.Getter;
import org.springframework.lang.Nullable;

/**
 * @author : Galaxy
 * @time : 2022/8/26 8:44
 * @create : IdeaJ
 * @project : server
 * @description : 第三方认证的类型枚举
 */
@Getter
public enum AuthType {

    /**
     * 默认方式，即 oauth 的标准登录方式，
     */
    PASSWORD("password", "密码登录"),
    CAPTCHA("captcha", "验证码");

    private final String code;
    private final String name;

    AuthType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public boolean equals(String authType) {
        return this.code.equalsIgnoreCase(authType);
    }

    public static AuthType getByCode(String code) {
        code = code.toLowerCase().trim();
        for (AuthType cur : AuthType.values()) {
            if (cur.code.equals(code)) {
                return cur;
            }
        }
        return PASSWORD;
    }
}
