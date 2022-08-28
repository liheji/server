package top.liheji.server.config.auth.service;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.base.Strings;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * @author : Galaxy
 * @time : 2022/8/25 8:11
 * @create : IdeaJ
 * @project : server
 * @description :
 */
public class QQOAuth2UserServiceImpl implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    /**
     * 获取用户 openId 并获取用户的信息
     */
    private static final String BASE_USER_INFO = "https://graph.qq.com/user/get_user_info?access_token={accessToken}&oauth_consumer_key={clientId}&openid={openId}";

    private RestTemplate restTemplate;

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
        }

        return restTemplate;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        // 第一步：获取openId接口响应
        String accessToken = oAuth2UserRequest.getAccessToken().getTokenValue();
        String openIdUrl = oAuth2UserRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri() + "?access_token={accessToken}&fmt=json";
        String result = getRestTemplate().getForObject(openIdUrl, String.class, accessToken);
        if (Strings.isNullOrEmpty(result)) {
            throw new OAuth2AuthenticationException("请求结果为空");
        }
        HashMap resultMap = JSONObject.parseObject(result, HashMap.class);
        // 提取openId
        String clientId = (String) resultMap.get("client_id");
        String openId = (String) resultMap.get("openid");

        // 第二步：获取用户信息
        String userResult = getRestTemplate().getForObject(BASE_USER_INFO, String.class, accessToken, clientId, openId);
        if (Strings.isNullOrEmpty(userResult)) {
            throw new OAuth2AuthenticationException("请求结果为空");
        }
        HashMap<String, Object> qqUserInfo = (HashMap<String, Object>) JSONObject.parseObject(userResult, HashMap.class);
        // 为用户信息类补充openId
        if (qqUserInfo == null) {
            throw new OAuth2AuthenticationException("用户信息获取失败");
        }
        qqUserInfo.put("openid", openId);

        return new DefaultOAuth2User(new LinkedHashSet<>(), qqUserInfo, "openid");
    }
}
