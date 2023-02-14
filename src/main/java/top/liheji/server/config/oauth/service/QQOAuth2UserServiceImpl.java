package top.liheji.server.config.oauth.service;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import top.liheji.server.util.BeanUtils;

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
     * 用户 openId key 和 获取链接
     */
    private static final String OPENID_URL = "https://graph.qq.com/oauth2.0/me?access_token={accessToken}&fmt=json";

    /**
     * 获取用户信息的 GET请求参数
     */
    private static final String USER_INFO_PARAMS = "?access_token={accessToken}&oauth_consumer_key={clientId}&openid={openId}";

    private RestTemplate restTemplate;

    private synchronized RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = BeanUtils.getBean(RestTemplate.class);
        }
        return restTemplate;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        final ClientRegistration clientRegistration = oAuth2UserRequest.getClientRegistration();
        final ClientRegistration.ProviderDetails.UserInfoEndpoint endpoint = clientRegistration.getProviderDetails().getUserInfoEndpoint();

        // 获取第三方服务的唯一标识的 key
        final String userNameAttributeName = endpoint.getUserNameAttributeName();
        final String accessToken = oAuth2UserRequest.getAccessToken().getTokenValue();

        // 第一步：获取openId接口响应
        String openidStr = getRestTemplate().getForObject(OPENID_URL, String.class, accessToken);
        JSONObject openidResult = JSONObject.parseObject(openidStr);
        if (ObjectUtils.isEmpty(openidResult) || openidResult.containsKey("code")) {
            throw new OAuth2AuthenticationException("获取QQ用户信息失败");
        }
        String openId = openidResult.getString(userNameAttributeName);

        // 第二步：获取用户信息
        String userInfoStr = getRestTemplate().getForObject(endpoint.getUri() + USER_INFO_PARAMS, String.class,
                accessToken, oAuth2UserRequest.getClientRegistration().getClientId(), openId);
        JSONObject userInfoResult = JSONObject.parseObject(userInfoStr);
        if (ObjectUtils.isEmpty(userInfoResult) || !userInfoResult.get("ret").equals(0)) {
            throw new OAuth2AuthenticationException("获取QQ用户信息失败");
        }
        userInfoResult.put(userNameAttributeName, openId);
        // 第三步：返回用户信息
        return new DefaultOAuth2User(new LinkedHashSet<>(), userInfoResult, userNameAttributeName);
    }
}
