package top.liheji.server.config.auth.client;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.base.Strings;
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
public class QQAuthorizationCodeTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {
    private RestTemplate restTemplate;

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
        }

        return restTemplate;
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
        ClientRegistration clientRegistration = authorizationCodeGrantRequest.getClientRegistration();
        OAuth2AuthorizationExchange oAuth2AuthorizationExchange = authorizationCodeGrantRequest.getAuthorizationExchange();

        // 根据API文档获取请求 access_token参数
        String baseURI = clientRegistration.getProviderDetails().getTokenUri() + "?grant_type=authorization_code&client_id={clientId}&client_secret={clientSecret}&code={code}&redirect_uri={redirectUri}&fmt=json";
        String token = getRestTemplate().getForObject(baseURI, String.class,
                clientRegistration.getClientId(),
                clientRegistration.getClientSecret(),
                oAuth2AuthorizationExchange.getAuthorizationResponse().getCode(),
                oAuth2AuthorizationExchange.getAuthorizationRequest().getRedirectUri()
        );

        if (Strings.isNullOrEmpty(token)) {
            throw new OAuth2AuthenticationException("请求结果为空");
        }

        HashMap tokenMap = JSONObject.parseObject(token, HashMap.class);
        String accessToken = (String) tokenMap.get("access_token");
        String refreshToken = (String) tokenMap.get("refresh_token");
        long expiresIn = Long.parseLong((String) tokenMap.get("expires_in"));

        Set<String> scopes = new LinkedHashSet<>(oAuth2AuthorizationExchange.getAuthorizationRequest().getScopes());
        Map<String, Object> additionalParameters = new LinkedHashMap<>();
        OAuth2AccessToken.TokenType accessTokenType = OAuth2AccessToken.TokenType.BEARER;

        return OAuth2AccessTokenResponse.withToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(accessTokenType)
                .expiresIn(expiresIn)
                .scopes(scopes)
                .additionalParameters(additionalParameters)
                .build();
    }
}
