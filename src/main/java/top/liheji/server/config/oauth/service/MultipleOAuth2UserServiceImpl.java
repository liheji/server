package top.liheji.server.config.oauth.service;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.ObjectUtils;
import top.liheji.server.config.oauth.constant.OAuthType;
import top.liheji.server.pojo.AuthAccount;
import top.liheji.server.util.BeanUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Galaxy
 * @time : 2022/8/25 8:11
 * @create : IdeaJ
 * @project : server
 * @description : 请求 QQ 的用户信息并返回 OAuth2User
 */
public class MultipleOAuth2UserServiceImpl implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private static final String USERNAME_KEY = "openId";

    private final Map<String, OAuth2UserService<OAuth2UserRequest, OAuth2User>> multipleService = new ConcurrentHashMap<>();

    public MultipleOAuth2UserServiceImpl() {
        this.put(OAuthType.DEFAULT, new DefaultOAuth2UserService());
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        final String rId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        OAuthType authType = OAuthType.getByCode(rId);
        if (ObjectUtils.isEmpty(authType)) {
            throw new DisabledException("暂不支持 " + rId + " 登录");
        }
        OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2Service = this.get(authType);
        if (ObjectUtils.isEmpty(oauth2Service)) {
            oauth2Service = this.get(OAuthType.DEFAULT);
        }

        OAuth2User oAuth2User = oauth2Service.loadUser(oAuth2UserRequest);
        // 第三方账号是否与系统账号绑定
        AuthAccount obj = new AuthAccount();
        obj.setOpenId(oAuth2User.getName());
        obj.setAuthCode(rId);
        switch (authType) {
            case QQ:
                obj.setName(oAuth2User.getAttribute("nickname"));
                obj.setAvatarUrl(oAuth2User.getAttribute("figureurl_qq_1"));
                break;
            case Gitee:
            case GitHub:
                obj.setName(oAuth2User.getAttribute("name"));
                obj.setAvatarUrl(oAuth2User.getAttribute("avatar_url"));
                break;
            case Baidu:
                obj.setName(oAuth2User.getAttribute("username"));
                obj.setAvatarUrl(oAuth2User.getAttribute("portrait"));
                break;
            default:
                throw new DisabledException("暂不支持" + rId + "登录");
        }

        Map<String, Object> map = BeanUtils.toMap(obj);
        return new DefaultOAuth2User(oAuth2User.getAuthorities(), map, USERNAME_KEY);
    }

    public void put(OAuthType authType, OAuth2UserService<OAuth2UserRequest, OAuth2User> service) {
        this.multipleService.put(authType.getCode(), service);
    }

    public OAuth2UserService<OAuth2UserRequest, OAuth2User> get(OAuthType authType) {
        return this.multipleService.get(authType.getCode());
    }
}
