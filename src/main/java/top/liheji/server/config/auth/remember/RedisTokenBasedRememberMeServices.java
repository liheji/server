package top.liheji.server.config.auth.remember;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.rememberme.*;
import org.springframework.util.ObjectUtils;
import top.liheji.server.config.auth.remember.impl.RedisTokenRepositoryImpl;
import top.liheji.server.constant.ServerConstant;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.AuthAccount;
import top.liheji.server.pojo.AuthDevices;
import top.liheji.server.service.AccountService;
import top.liheji.server.service.AuthAccountService;
import top.liheji.server.service.AuthDevicesService;
import top.liheji.server.util.R;
import top.liheji.server.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : Galaxy
 * @time : 2023/2/12 19:06
 * @create : IdeaJ
 * @project : server
 * @description :
 */
@Slf4j
public class RedisTokenBasedRememberMeServices extends PersistentTokenBasedRememberMeServices {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthAccountService authAccountService;

    @Autowired
    private AuthDevicesService authDevicesService;

    @Autowired
    private RedisTokenRepositoryImpl tokenRepository;

    public RedisTokenBasedRememberMeServices(String key, UserDetailsService userDetailsService, PersistentTokenRepository tokenRepository) {
        super(key, userDetailsService, tokenRepository);
    }

    @Override
    protected UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request, HttpServletResponse response) {
        try {
            UserDetails details = super.processAutoLoginCookie(cookieTokens, request, response);
            String username = details.getUsername();
            // 新的序列号
            String series = tokenRepository.getSeriesForUsername(username);
            // 设置设备信息
            this.authDevicesService.update(
                    new LambdaUpdateWrapper<AuthDevices>()
                            .set(AuthDevices::getSeries, series)
                            .eq(AuthDevices::getSeries, cookieTokens[0])
                            .eq(AuthDevices::getUsername, username)
            );
            return details;
        } catch (CookieTheftException e) {
            // 清除登录信息
            this.authDevicesService.update(
                    new LambdaUpdateWrapper<AuthDevices>()
                            .set(AuthDevices::getSeries, "")
                            .eq(AuthDevices::getSeries, cookieTokens[0])
            );
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 覆盖登录成功的方法
     * 1. 设置第三方登录的信息
     * 2. 设置持久化设备信息
     *
     * @param request                  请求
     * @param response                 响应
     * @param successfulAuthentication 认证信息
     */
    @Override
    protected void onLoginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        String username = successfulAuthentication.getName();

        // Oauth2认证
        if (successfulAuthentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oAuth2Token = (OAuth2AuthenticationToken) successfulAuthentication;
            // 检查 Cookie
            Account account = ServerConstant.LOCAL_ACCOUNT.get();
            // 查询是否存在
            AuthAccount obj = authAccountService.getOne(
                    new LambdaQueryWrapper<AuthAccount>()
                            .eq(AuthAccount::getOpenId, username)
            );
            try {
                Map<String, Object> user = oAuth2Token.getPrincipal().getAttributes();
                if (account != null) {
                    if (obj == null) {
                        // 绑定用户
                        obj = new AuthAccount();
                        BeanUtils.copyProperties(user, obj);
                        obj.setAccountId(account.getId());
                        // 保存认证
                        authAccountService.save(obj);
                    }
                } else {
                    if (obj == null) {
                        throw new RememberMeAuthenticationException("未绑定该第三方账号");
                    }
                    account = accountService.getById(obj.getAccountId());
                }
                Map<String, Object> map = new HashMap<>(user);
                // 不管是绑定账号还是登录，因为均进行了登录的操作，所以需要重新设置登录信息
                map.put("username", account.getUsername());
                map.put("accountId", account.getId());
                UserDetails details = getUserDetailsService().loadUserByUsername(account.getUsername());
                // 设置认证权限
                SecurityContextHolder.getContext().setAuthentication(
                        new OAuth2AuthenticationToken(
                                new DefaultOAuth2User(details.getAuthorities(), map, "username"),
                                details.getAuthorities(),
                                oAuth2Token.getAuthorizedClientRegistrationId()
                        )
                );
                // 执行登录设备认证
                checkAuthDevice(request, response, account.getUsername());
                // 跳转页面
                WebUtils.response(response, "", true);
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    WebUtils.response(response, R.error(ex.getMessage()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            // oauth 认证完成
        }

        checkAuthDevice(request, response, username);
    }

    private void checkAuthDevice(HttpServletRequest request, HttpServletResponse response, String username) {
        log.debug(String.format("Creating new persistent login for user %s", username));

        Date curDate = new Date();
        PersistentRememberMeToken persistentToken = new PersistentRememberMeToken(username, generateSeriesData(), generateTokenData(), new Date());
        try {
            AuthDevices device = ServerConstant.LOCAL_DEVICE.get();
            device.setUsername(username);
            device.setSeries(persistentToken.getSeries());
            device.setLastUsed(curDate);
            String paramValue = request.getParameter(getParameter());
            if (ObjectUtils.isEmpty(paramValue) || "false".equalsIgnoreCase(paramValue) || "0".equals(paramValue)) {
                // 不记住密码
                persistentToken = new PersistentRememberMeToken(username, persistentToken.getSeries(),
                        persistentToken.getTokenValue(), new Date(0L));
            }
            this.tokenRepository.createNewToken(persistentToken);
            addCookie(persistentToken, request, response);

            this.authDevicesService.saveOrUpdate(device,
                    new LambdaQueryWrapper<AuthDevices>()
                            .eq(AuthDevices::getType, device.getType())
                            .eq(AuthDevices::getUsername, username)
            );

            Account cur = accountService.getOne(
                    new LambdaQueryWrapper<Account>()
                            .eq(Account::getUsername, username)
            );
            // 更新登录日期
            cur.setLastLogin(curDate);
            accountService.updateById(cur);

            ServerConstant.LOCAL_ACCOUNT.set(cur);
            ServerConstant.LOCAL_SERIES.set(persistentToken.getSeries());
        } catch (Exception ex) {
            log.info("Failed to save persistent token " + ex.getMessage());
        }
    }

    private void addCookie(PersistentRememberMeToken token, HttpServletRequest request, HttpServletResponse response) {
        setCookie(new String[]{token.getSeries(), token.getTokenValue()}, getTokenValiditySeconds(), request,
                response);
    }
}
