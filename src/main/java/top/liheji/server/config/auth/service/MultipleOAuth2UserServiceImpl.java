package top.liheji.server.config.auth.service;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import top.liheji.server.config.auth.AuthType;
import top.liheji.server.pojo.AuthAccount;

import java.util.*;

/**
 * @author : Galaxy
 * @time : 2022/8/25 8:11
 * @create : IdeaJ
 * @project : server
 * @description : 请求 QQ 的用户信息并返回 OAuth2User
 */
public class MultipleOAuth2UserServiceImpl implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final HashMap<String, OAuth2UserService<OAuth2UserRequest, OAuth2User>> multipleService;

    private final String defaultServiceKey = "default_service";

    public MultipleOAuth2UserServiceImpl() {
        this.multipleService = new HashMap<>();
        multipleService.put(defaultServiceKey, new DefaultOAuth2UserService());
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        ClientRegistration clientRegistration = oAuth2UserRequest.getClientRegistration();
        final String rId = clientRegistration.getRegistrationId();

        OAuth2UserService<OAuth2UserRequest, OAuth2User> service = multipleService.get(rId);
        if (service == null) {
            service = multipleService.get(defaultServiceKey);
        }

        AuthType authType = AuthType.getByCode(rId);
        if (authType == null) {
            throw new DisabledException("暂不支持 " + rId + " 登录");
        }
        OAuth2User oAuth2User = service.loadUser(oAuth2UserRequest);

        // 第三方账号是否与系统账号绑定
        AuthAccount obj = new AuthAccount();
        obj.setOpenId(oAuth2User.getName());
        obj.setAuthCode(rId);
        switch (authType) {
            case QQ:
                obj.setName(oAuth2User.getAttribute("nickname"));
                obj.setAvatarUrl(oAuth2User.getAttribute("figureurl_qq_1"));
                break;
            case Weibo:
                obj.setName(oAuth2User.getAttribute("screen_name"));
                obj.setAvatarUrl(oAuth2User.getAttribute("avatar_large"));
                break;
            case WeChat:
                obj.setName(oAuth2User.getAttribute("nickname"));
                obj.setAvatarUrl(oAuth2User.getAttribute("headimgurl"));
                break;
            case GitHub:
                obj.setName(oAuth2User.getAttribute("name"));
                obj.setAvatarUrl(oAuth2User.getAttribute("avatar_url"));
                break;
            default:
                throw new DisabledException("暂不支持" + rId + "登录");
        }

        return new DefaultOAuth2User(oAuth2User.getAuthorities(), obj.objToMap(), "openId");
    }

    public HashMap<String, OAuth2UserService<OAuth2UserRequest, OAuth2User>> getMultipleService() {
        return multipleService;
    }
}
