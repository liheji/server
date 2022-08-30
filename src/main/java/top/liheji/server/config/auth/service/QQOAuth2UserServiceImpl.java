package top.liheji.server.config.auth.service;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.base.Strings;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
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
     * 获取用户 openId
     */
    private static final String OPENID_URL = "https://graph.qq.com/oauth2.0/me?access_token={accessToken}&fmt=json";

    /**
     * 获取用户信息的 GET请求参数
     */
    private static final String USER_INFO_PARAMS = "?access_token={accessToken}&oauth_consumer_key={clientId}&openid={openId}";

    private RestTemplate restTemplate;

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
        }
        return restTemplate;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        final String accessToken = oAuth2UserRequest.getAccessToken().getTokenValue();
        final ClientRegistration.ProviderDetails.UserInfoEndpoint endpoint = oAuth2UserRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint();
        // 第一步：获取openId接口响应
        String openidResult = getRestTemplate().getForObject(OPENID_URL, String.class, accessToken);
        if (Strings.isNullOrEmpty(openidResult)) {
            throw new OAuth2AuthenticationException("请求结果为空");
        }
        HashMap openidMap = JSONObject.parseObject(openidResult, HashMap.class);
        if (openidMap.containsKey("msg") && openidMap.containsKey("code")) {
            throw new OAuth2AuthenticationException("接口调用返回错误");
        }
        String openId = openidMap.get("openid").toString();

        // 第二步：获取用户信息
        String userInfoResult = getRestTemplate().getForObject(endpoint.getUri() + USER_INFO_PARAMS, String.class,
                accessToken,
                oAuth2UserRequest.getClientRegistration().getClientId(),
                openId
        );
        if (Strings.isNullOrEmpty(userInfoResult)) {
            throw new OAuth2AuthenticationException("请求结果为空");
        }
        HashMap<String, Object> userInfoMap = (HashMap<String, Object>) JSONObject.parseObject(userInfoResult, HashMap.class);
        if ((Integer) userInfoMap.get("ret") != 0) {
            throw new OAuth2AuthenticationException("接口调用返回错误");
        }
        userInfoMap.put("openid", openId);

        return new DefaultOAuth2User(new LinkedHashSet<>(), userInfoMap, endpoint.getUserNameAttributeName());
    }
}
