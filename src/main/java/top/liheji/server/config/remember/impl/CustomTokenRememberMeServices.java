package top.liheji.server.config.remember.impl;

/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.log.LogMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.*;
import org.springframework.util.Assert;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.PersistentDevices;
import top.liheji.server.pojo.PersistentLogins;
import top.liheji.server.service.PersistentDevicesService;
import top.liheji.server.service.PersistentLoginsService;
import top.liheji.server.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

/**
 * @author : Galaxy
 * @time : 2022/3/5 16:54
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 重写记住密码功能
 * 增加记住登录设备功能
 * 使用 MybatisPlus操作数据库
 */
public class CustomTokenRememberMeServices extends top.liheji.server.config.remember.CustomAbstractRememberMeServices {

    @Autowired
    private PersistentLoginsService persistentLoginsService;

    @Autowired
    private PersistentDevicesService persistentDevicesService;

    private final SecureRandom random;

    public static final int DEFAULT_SERIES_LENGTH = 16;

    public static final int DEFAULT_TOKEN_LENGTH = 16;

    private int seriesLength = DEFAULT_SERIES_LENGTH;

    private int tokenLength = DEFAULT_TOKEN_LENGTH;

    public CustomTokenRememberMeServices(String key, UserDetailsService userDetailsService) {
        //设置默认参数
        super(key, userDetailsService);
        this.random = new SecureRandom();
        super.setAlwaysRemember(true);
    }

    /**
     * Locates the presented cookie data in the token repository, using the series id. If
     * the data compares successfully with that in the persistent store, a new token is
     * generated and stored with the same series. The corresponding cookie value is set on
     * the response.
     *
     * @param cookieTokens the series and token values
     * @throws RememberMeAuthenticationException if there is no stored token corresponding
     *                                           to the submitted cookie, or if the token in the persistent store has expired.
     * @throws InvalidCookieException            if the cookie doesn't have two tokens as expected.
     * @throws CookieTheftException              if a presented series value is found, but the stored
     *                                           token is different from the one presented.
     */
    @Override
    protected UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request,
                                                 HttpServletResponse response) {
        if (cookieTokens.length != 2) {
            throw new InvalidCookieException("Cookie token did not contain " + 2 + " tokens, but contained '"
                    + Arrays.asList(cookieTokens) + "'");
        }
        String presentedSeries = cookieTokens[0];
        String presentedToken = cookieTokens[1];
        PersistentLogins token = persistentLoginsService.getById(presentedSeries);
        if (token == null) {
            // No series match, so we can't authenticate using this cookie
            throw new RememberMeAuthenticationException("No persistent token found for series id: " + presentedSeries);
        }
        // We have a match for this user/series combination
        if (!presentedToken.equals(token.getToken())) {
            // Token doesn't match series value. Delete all logins for this user and throw
            // an exception to warn them.
            PersistentDevices devices = new PersistentDevices();
            devices.setSeries("");
            this.persistentDevicesService.update(
                    devices,
                    new LambdaQueryWrapper<PersistentDevices>()
                            .eq(PersistentDevices::getSeries, token.getSeries())
            );
            this.persistentLoginsService.removeById(token.getSeries());
            throw new CookieTheftException(this.messages.getMessage(
                    "PersistentTokenBasedRememberMeServices.cookieStolen",
                    "Invalid remember-me token (Series/token) mismatch. Implies previous cookie theft attack."));
        }

        if (token.getLastUsed().getTime() == 0 ||
                token.getLastUsed().getTime() + getTokenValiditySeconds() * 1000L < System.currentTimeMillis()) {
            throw new RememberMeAuthenticationException("Remember-me login has expired");
        }

        // Token also matches, so login is valid. Update the token value, keeping the
        // *same* series number.
        this.logger.debug(LogMessage.format("Refreshing persistent login token for user '%s', series '%s'",
                token.getUsername(), token.getSeries()));
        PersistentLogins newToken = new PersistentLogins(token.getUsername(), token.getSeries(),
                generateTokenData(), new Date());

        try {
            this.persistentLoginsService.updateById(newToken);
            addCookie(newToken, request, response);
            PersistentDevices device = WebUtils.parseAgent(request);
            if (device != null) {
                device.setUsername(newToken.getUsername());
                device.setSeries(newToken.getSeries());
                device.setLastUsed(new Date());
                this.persistentDevicesService.update(device,
                        new LambdaQueryWrapper<PersistentDevices>()
                                .eq(PersistentDevices::getType, device.getType())
                                .eq(PersistentDevices::getUsername, device.getUsername())
                );
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            }
        } catch (Exception ex) {
            this.logger.error("Failed to update token: ", ex);
            throw new RememberMeAuthenticationException("Autologin failed due to data access problem");
        }
        return getUserDetailsService().loadUserByUsername(token.getUsername());
    }

    /**
     * Creates a new persistent login token with a new series number, stores the data in
     * the persistent token repository and adds the corresponding cookie to the response.
     */
    @Override
    protected void onLoginSuccess(HttpServletRequest request, HttpServletResponse response,
                                  Authentication successfulAuthentication) {

        String username = successfulAuthentication.getName();
        this.logger.debug(LogMessage.format("Creating new persistent login for user %s", username));
        Date curDate = new Date();
        PersistentLogins persistentToken = new PersistentLogins(username, generateSeriesData(),
                generateTokenData(), curDate);
        try {
            //************* 修改此处 ***************
            PersistentDevices device = WebUtils.parseAgent(request);
            if (device != null) {
                device.setUsername(username);
                device.setSeries(persistentToken.getSeries());
                device.setLastUsed(curDate);
                String paramValue = request.getParameter(getParameter());
                if (paramValue != null) {
                    if (!paramValue.equalsIgnoreCase("true") && !paramValue.equals("1")) {
                        persistentToken = new PersistentLogins(username, persistentToken.getSeries(),
                                persistentToken.getToken(), new Date(0L));
                    }
                }
                this.persistentLoginsService.save(persistentToken);
                addCookie(persistentToken, request, response);

                this.persistentDevicesService.saveOrUpdate(device,
                        new LambdaQueryWrapper<PersistentDevices>()
                                .eq(PersistentDevices::getType, device.getType())
                                .eq(PersistentDevices::getUsername, username)
                );

                this.persistentLoginsService.remove(
                        new LambdaQueryWrapper<PersistentLogins>()
                                .eq(PersistentLogins::getUsername, username)
                                .notIn(PersistentLogins::getSeries,
                                        this.persistentDevicesService.listObjs(
                                                new LambdaQueryWrapper<PersistentDevices>()
                                                        .select(PersistentDevices::getSeries)
                                                        .eq(PersistentDevices::getUsername, username)
                                        )
                                )
                );

                Account cur = accountService.getOne(new LambdaQueryWrapper<Account>().eq(Account::getUsername, username));
                if (cur != null) {
                    cur.setLastLogin(curDate);
                    accountService.updateById(cur);
                    cur.setPassword("");
                    request.setAttribute("account", cur);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            }
        } catch (Exception ex) {
            this.logger.error("Failed to save persistent token ", ex);
        }
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            try {
                String rememberMeCookie = extractRememberMeCookie(request);
                if (rememberMeCookie == null || rememberMeCookie.length() == 0) {
                    return;
                }

                String[] cookieTokens = decodeCookie(rememberMeCookie);
                PersistentLogins token = this.persistentLoginsService.getById(cookieTokens[0]);
                PersistentDevices emptyDevices = new PersistentDevices();
                emptyDevices.setSeries("");
                if (token != null) {
                    this.persistentLoginsService.removeById(token.getSeries());
                    this.persistentDevicesService.update(
                            emptyDevices,
                            new LambdaQueryWrapper<PersistentDevices>()
                                    .eq(PersistentDevices::getSeries, token.getSeries())
                    );
                }

                PersistentDevices device = WebUtils.parseAgent(request);
                if (device != null) {
                    this.persistentDevicesService.update(emptyDevices,
                            new LambdaQueryWrapper<PersistentDevices>()
                                    .eq(PersistentDevices::getType, device.getType())
                                    .eq(PersistentDevices::getUsername, authentication.getName())
                    );
                }

                this.persistentLoginsService.remove(new LambdaQueryWrapper<PersistentLogins>()
                        .eq(PersistentLogins::getUsername, authentication.getName())
                        .notIn(PersistentLogins::getSeries,
                                this.persistentDevicesService.listObjs(
                                        new LambdaQueryWrapper<PersistentDevices>()
                                                .select(PersistentDevices::getSeries)
                                                .eq(PersistentDevices::getUsername, authentication.getName())
                                )
                        )
                );
            } catch (Exception ignored) {
            }
        }
        super.logout(request, response, authentication);
    }

    protected String generateSeriesData() {
        byte[] newSeries = new byte[this.seriesLength];
        this.random.nextBytes(newSeries);
        return new String(Base64.getEncoder().encode(newSeries));
    }

    protected String generateTokenData() {
        byte[] newToken = new byte[this.tokenLength];
        this.random.nextBytes(newToken);
        return new String(Base64.getEncoder().encode(newToken));
    }

    private void addCookie(PersistentLogins token, HttpServletRequest request, HttpServletResponse response) {
        setCookie(new String[]{token.getSeries(), token.getToken()}, getTokenValiditySeconds(), request,
                response);
    }

    public void setSeriesLength(int seriesLength) {
        this.seriesLength = seriesLength;
    }

    public void setTokenLength(int tokenLength) {
        this.tokenLength = tokenLength;
    }

    @Override
    public void setTokenValiditySeconds(int tokenValiditySeconds) {
        Assert.isTrue(tokenValiditySeconds > 0, "tokenValiditySeconds must be positive for this implementation");
        super.setTokenValiditySeconds(tokenValiditySeconds);
    }
}
