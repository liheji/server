package top.yilee.server.config.oauth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import org.springframework.util.MultiValueMap;
import top.dcenter.ums.security.core.oauth.entity.ConnectionData;
import top.dcenter.ums.security.core.oauth.entity.ConnectionDto;
import top.dcenter.ums.security.core.oauth.exception.RegisterUserFailureException;
import top.dcenter.ums.security.core.oauth.repository.exception.UpdateConnectionException;
import top.dcenter.ums.security.core.oauth.service.UmsUserDetailsService;
import top.dcenter.ums.security.core.oauth.signup.ConnectionService;
import top.yilee.server.constant.ServerConstant;
import top.yilee.server.pojo.Account;
import top.yilee.server.pojo.AuthAccount;
import top.yilee.server.service.AccountService;
import top.yilee.server.service.AuthAccountService;
import top.yilee.server.util.JsonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Repository
public class OAuthConnectionServiceImpl implements ConnectionService {
    @Autowired
    UmsUserDetailsService umsUserDetailsService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthAccountService authAccountService;

    @Override
    public UserDetails signUp(AuthUser authUser, String providerId, String encodeState) throws RegisterUserFailureException {
        return null;  // 无注册功能，无需实现
    }

    @Override
    public void updateUserConnectionAndAuthToken(AuthUser authUser, ConnectionData connectionData) throws UpdateConnectionException {
        // 无需更新 token，无需实现
    }

    @Override
    public void binding(UserDetails principal, AuthUser authUser, String providerId) {
        Account account = ServerConstant.LOCAL_ACCOUNT.get();
        // 绑定
        AuthAccount authAccount = new AuthAccount();
        authAccount.setAccountId(account.getId());
        authAccount.setAuthCode(providerId);
        authAccount.setOpenId(authUser.getUuid());
        authAccount.setUsername(authUser.getUsername());
        authAccount.setAvatarUrl(authUser.getAvatar());
        authAccount.setName(authUser.getNickname());
        authAccount.setAuthToken(JsonUtils.toJSONString(authUser.getToken()));
        authAccountService.save(authAccount);
    }

    @Override
    public void unbinding(String userId, String providerId, String providerUserId) {
        Account account = ServerConstant.LOCAL_ACCOUNT.get();

        if (userId == null || !userId.equals(account.getUsername())) {
            return;
        }

        authAccountService.remove(new LambdaQueryWrapper<AuthAccount>().eq(AuthAccount::getAccountId, account.getId()).eq(AuthAccount::getAuthCode, providerId).eq(AuthAccount::getOpenId, providerUserId));
    }

    @Override
    public List<ConnectionData> findConnectionByProviderIdAndProviderUserId(String providerId, String providerUserId) {
        AuthAccount authOne = authAccountService.getOne(new LambdaQueryWrapper<AuthAccount>().eq(AuthAccount::getAuthCode, providerId).eq(AuthAccount::getOpenId, providerUserId));

        if (authOne == null) {
            return new ArrayList<>();
        }

        Account account = accountService.getById(authOne.getAccountId());
        ConnectionData data = ConnectionData.builder().userId(account.getUsername()).providerId(providerId).providerUserId(providerUserId).displayName(authOne.getName()).imageUrl(authOne.getAvatarUrl()).build();
        return Collections.singletonList(data);
    }

    @Override
    public MultiValueMap<String, ConnectionDto> listAllConnections(String userId) {
        // 暂不知在哪里使用
        return null;
    }
}
