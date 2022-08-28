package top.liheji.server.config.auth.client;

import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import java.util.*;

/**
 * @author : Galaxy
 * @time : 2022/8/24 23:34
 * @create : IdeaJ
 * @project : server
 * @description : 使用code交换 access_token的具体逻辑，兼容模式，可以兼容非标准的 Oauth
 */
public class MultipleAuthorizationCodeTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {
    private final HashMap<String, OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>> multipleClient;

    private final String defaultClientKey = "default_client";

    public MultipleAuthorizationCodeTokenResponseClient() {
        this.multipleClient = new HashMap<>();
        multipleClient.put(defaultClientKey, new DefaultAuthorizationCodeTokenResponseClient());
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
        ClientRegistration clientRegistration = authorizationCodeGrantRequest.getClientRegistration();
        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> client = multipleClient.get(clientRegistration.getRegistrationId());
        if (client == null) {
            client = multipleClient.get(defaultClientKey);
        }

        return client.getTokenResponse(authorizationCodeGrantRequest);
    }

    public HashMap<String, OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>> getMultipleClient() {
        return multipleClient;
    }
}
