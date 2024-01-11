package top.liheji.server.config.oauth.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author : Galaxy
 * @time : 2022/8/26 8:44
 * @create : IdeaJ
 * @project : server
 * @description : 第三方认证的类型枚举
 */
@Getter
public enum OAuthType {
    /**
     * 默认方式，即 oauth 的标准登录方式，
     */
    DEFAULT("default", "Default"),
    QQ("qq", "QQ"),
    Baidu("baidu", "百度"),
    GitHub("github", "GitHub"),
    Weibo("weibo", "微博", false),
    Gitee("gitee", "Gitee", false),
    Google("google", "谷歌", false),
    Huawei("huawei", "华为", false),
    Xiaomi("xiaomi", "小米", false),
    WeChat("wechat", "微信", false);

    private final String code;
    private final String name;
    private final Boolean enabled;

    OAuthType(String code, String name) {
        this.code = code;
        this.name = name;
        this.enabled = true;
    }

    OAuthType(String code, String name, Boolean enabled) {
        this.code = code;
        this.name = name;
        this.enabled = enabled;
    }

    public static OAuthType getByCode(String code) {
        code = code.toLowerCase().trim();
        for (OAuthType cur : OAuthType.values()) {
            if (cur.code.equals(code)) {
                return cur;
            }
        }
        return null;
    }

    public static List<OAuthType> available() {
        return Arrays.asList(QQ, Baidu, GitHub);
    }
}
