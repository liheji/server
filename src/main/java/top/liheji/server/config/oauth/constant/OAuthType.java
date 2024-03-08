package top.liheji.server.config.oauth.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    DEFAULT("default", "默认", false),
    ALIPAY("alipay", "支付宝", false),
    ALIYUN("aliyun", "阿里云", false),
    BAIDU("baidu", "百度", false),
    CODING("coding", "Coding", false),
    CSDN("csdn", "CSDN", false),
    DINGTALK("dingtalk", "钉钉", false),
    DOUYIN("douyin", "抖音", false),
    ELEME("eleme", "饿了么", false),
    FACEBOOK("facebook", "Facebook", false),
    FEISHU("feishu", "飞书", false),
    GITEE("gitee", "Gitee", true),
    GITHUB("github", "GitHub", true),
    GITLAB("gitlab", "GitLab", false),
    GOOGLE("google", "Google", false),
    HUAWEI("huawei", "华为", false),
    JD("jd", "京东", false),
    KUJIALE("kujiale", "酷家乐", false),
    LINKEDIN("linkedin", "领英", false),
    MEITUAN("meituan", "美团", false),
    MI("mi", "小米", false),
    MICROSOFT("microsoft", "微软", false),
    OSCHINA("oschina", "开源中国", false),
    PINTEREST("pinterest", "Pinterest", false),
    QQ("qq", "QQ", true),
    RENREN("renren", "人人网", false),
    STACK_OVERFLOW("stack_overflow", "Stack Overflow", false),
    TAOBAO("taobao", "淘宝", false),
    TEAMBITION("teambition", "Teambition", false),
    TOUTIAO("toutiao", "今日头条", false),
    TWITTER("twitter", "Twitter", false),
    WECHAT_ENTERPRISE("wechat_enterprise", "企业微信二维码登录", false),
    WECHAT_ENTERPRISE_WEB("wechat_enterprise_web", "企业微信网页登录", false),
    WECHAT_MP("wechat_mp", "微信公众平台", false),
    WECHAT_OPEN("wechat_open", "微信开放平台", false),
    WEIBO("weibo", "新浪微博", false),
    XMLY("xmly", "喜马拉雅", false);


    private final String code;
    private final String name;
    private final Boolean enabled;

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
        List<OAuthType> oAuthTypeList = Arrays.stream(OAuthType.values())
                .filter(OAuthType::getEnabled)
                .collect(Collectors.toList());
        return oAuthTypeList;
    }
}
