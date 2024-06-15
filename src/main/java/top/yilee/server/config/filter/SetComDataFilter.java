package top.yilee.server.config.filter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import top.yilee.server.config.auth.constant.AuthConstant;
import top.yilee.server.constant.ServerConstant;
import top.yilee.server.pojo.Account;
import top.yilee.server.pojo.AuthDevices;
import top.yilee.server.service.AccountService;
import top.yilee.server.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author : Galaxy
 * @time : 2022/1/30 8:59
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 系统登录前自动设置 UserAgent到共享数据
 */
@Component
public class SetComDataFilter extends OncePerRequestFilter {
    @Autowired
    private AccountService accountService;

    @Autowired
    PersistentTokenRepository redisTokenRepository;

    public SetComDataFilter() {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 验证登录设备信息
        AuthDevices device = WebUtils.parseAgent(request);
        if (ObjectUtils.isEmpty(device)) {
            throw new DisabledException("无法识别的访问设备");
        }
        ServerConstant.LOCAL_DEVICE.set(device);

        //设置参数
        String cookieValue = extractRememberMeCookie(request);
        if (cookieValue != null) {
            String[] strings = decodeCookie(cookieValue);
            if (strings != null) {
                PersistentRememberMeToken token = redisTokenRepository.getTokenForSeries(strings[0]);
                if (token == null) {
                    request.getSession().invalidate();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                Account account = accountService.getOne(
                        new LambdaQueryWrapper<Account>()
                                .eq(Account::getUsername, token.getUsername())
                );
                // 共享数据
                ServerConstant.LOCAL_ACCOUNT.set(account);
                ServerConstant.LOCAL_SERIES.set(strings[0]);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String[] decodeCookie(String cookieValue) throws InvalidCookieException {
        try {
            for (int j = 0; j < cookieValue.length() % 4; ++j) {
                cookieValue = cookieValue + "=";
            }
            String cookieAsPlainText;
            cookieAsPlainText = new String(Base64.getDecoder().decode(cookieValue.getBytes()));
            String[] tokens = org.springframework.util.StringUtils.delimitedListToStringArray(cookieAsPlainText, ":");
            for (int i = 0; i < tokens.length; ++i) {
                tokens[i] = URLDecoder.decode(tokens[i], StandardCharsets.UTF_8.toString());
            }
            return tokens;
        } catch (Exception var7) {
            return null;
        }
    }

    private String extractRememberMeCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if ((cookies == null) || (cookies.length == 0)) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (AuthConstant.REMEMBER_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
