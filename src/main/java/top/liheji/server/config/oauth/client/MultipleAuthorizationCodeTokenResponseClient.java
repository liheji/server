package top.liheji.server.config.oauth.client;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.util.ObjectUtils;
import top.liheji.server.config.oauth.constant.OAuthType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Galaxy
 * @time : 2022/8/24 23:34
 * @create : IdeaJ
 * @project : server
 * @description : 使用code交换 access_token的具体逻辑，兼容模式，可以兼容非标准的 Oauth
 */
public class MultipleAuthorizationCodeTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {
    private final Map<String, OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>> multipleClient = new ConcurrentHashMap<>();

    public MultipleAuthorizationCodeTokenResponseClient() {
        this.put(OAuthType.DEFAULT, new DefaultAuthorizationCodeTokenResponseClient());
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
        final String rId = authorizationCodeGrantRequest.getClientRegistration().getRegistrationId();
        OAuthType authType = OAuthType.getByCode(rId);
        if (ObjectUtils.isEmpty(authType)) {
            throw new DisabledException("暂不支持 " + rId + " 登录");
        }
        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> client = this.get(authType);
        if (ObjectUtils.isEmpty(client)) {
            client = this.get(OAuthType.DEFAULT);
        }

        return client.getTokenResponse(authorizationCodeGrantRequest);
    }

    public void put(OAuthType authType, OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> service) {
        this.multipleClient.put(authType.getCode(), service);
    }

    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> get(OAuthType authType) {
        return this.multipleClient.get(authType.getCode());
    }
}
