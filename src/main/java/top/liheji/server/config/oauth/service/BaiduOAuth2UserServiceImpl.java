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

import java.util.LinkedHashSet;

/**
 * @author : Galaxy
 * @time : 2022/8/25 8:11
 * @create : IdeaJ
 * @project : server
 * @description :
 */
public class BaiduOAuth2UserServiceImpl implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    /**
     * 百度图片链接需要拼接的前缀
     */
    private static final String PORTRAIT_PREFIX = "http://tb.himg.baidu.com/sys/portrait/item/";

    /**
     * 获取用户信息的 GET请求参数
     */
    private static final String USER_INFO_PARAMS = "?access_token={accessToken}";

    private RestTemplate restTemplate;

    private synchronized RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
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

        // 第一步：获取用户信息
        String userInfoStr = getRestTemplate().getForObject(endpoint.getUri() + USER_INFO_PARAMS, String.class, accessToken);
        JSONObject userInfoResult = JSONObject.parseObject(userInfoStr);
        if (ObjectUtils.isEmpty(userInfoResult) || userInfoResult.containsKey("error_code")) {
            throw new OAuth2AuthenticationException("获取百度用户信息失败");
        }
        userInfoResult.replace("portrait", PORTRAIT_PREFIX + userInfoResult.getString("portrait"));
        // 第二步：返回用户信息
        return new DefaultOAuth2User(new LinkedHashSet<>(), userInfoResult, userNameAttributeName);
    }
}
