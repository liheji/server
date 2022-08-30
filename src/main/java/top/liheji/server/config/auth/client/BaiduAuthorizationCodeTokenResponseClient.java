package top.liheji.server.config.auth.client;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * @author : Galaxy
 * @time : 2022/8/24 23:34
 * @create : IdeaJ
 * @project : server
 * @description : 使用code交换 access_token的具体逻辑
 */
public class BaiduAuthorizationCodeTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    /**
     * 获取用户 access_token 的GET请求参数
     */
    private static final String TOKEN_PARAMS = "?grant_type=authorization_code&client_id={clientId}&client_secret={clientSecret}&code={code}&redirect_uri={redirectUri}";

    private RestTemplate restTemplate;

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
        }

        return restTemplate;
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
        final ClientRegistration registration = authorizationCodeGrantRequest.getClientRegistration();
        final OAuth2AuthorizationExchange exchange = authorizationCodeGrantRequest.getAuthorizationExchange();
        // 根据API文档获取请求 access_token参数
        final String accessTokenUrl = registration.getProviderDetails().getTokenUri() + TOKEN_PARAMS;
        String accessTokenResult = getRestTemplate().getForObject(accessTokenUrl, String.class,
                registration.getClientId(),
                registration.getClientSecret(),
                exchange.getAuthorizationResponse().getCode(),
                exchange.getAuthorizationRequest().getRedirectUri()
        );

        if (Strings.isNullOrEmpty(accessTokenResult)) {
            throw new OAuth2AuthenticationException("请求结果为空");
        }
        HashMap accessTokenMap = JSONObject.parseObject(accessTokenResult, HashMap.class);
        if (accessTokenMap.containsKey("error")) {
            throw new OAuth2AuthenticationException("接口调用返回错误");
        }
        String accessToken = accessTokenMap.get("access_token").toString();
        String refreshToken = accessTokenMap.get("refresh_token").toString();
        long expiresIn = Long.parseLong(accessTokenMap.get("expires_in").toString());
        return OAuth2AccessTokenResponse.withToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(expiresIn)
                .scopes(exchange.getAuthorizationRequest().getScopes())
                .additionalParameters(new LinkedHashMap<>())
                .build();
    }
}
