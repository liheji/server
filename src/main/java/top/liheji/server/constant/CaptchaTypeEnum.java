package top.liheji.server.constant;

import lombok.Getter;

/**
 * @author : Galaxy
 * @time : 2023/2/11 13:29
 * @create : IdeaJ
 * @project : server
 * @description :
 */
@Getter
public enum CaptchaTypeEnum {
    EMAIL(10, "email", "邮箱验证码"),
    MOBILE(10, "mobile", "手机验证码"),
    GENERAL(5, "general", "通用验证码"),
    GENERAL_SECRET(5, "general_secret", "普通连接秘钥"),
    REGISTER_SECRET(60 * 24 * 3, "register_secret", "注册秘钥");

    /**
     * 过期时间
     */
    private final long expire;
    /**
     * 含义
     */
    private final String code;
    /**
     * 含义
     */
    private final String msg;


    CaptchaTypeEnum(long expire, String code, String msg) {
        this.expire = expire;
        this.code = code;
        this.msg = msg;
    }

    public static CaptchaTypeEnum from(String code) {
        for (CaptchaTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
