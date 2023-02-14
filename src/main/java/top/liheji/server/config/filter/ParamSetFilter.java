package top.liheji.server.config.filter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import top.liheji.server.config.auth.constant.AuthConstant;
import top.liheji.server.constant.ServerConstant;
import top.liheji.server.pojo.Account;
import top.liheji.server.service.AccountService;


import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

/**
 * @author : Galaxy
 * @time : 2022/1/30 8:59
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 系统登录后自动设置系统用户到 HttpServletRequest 的 Attribute中
 */
@Component
public class ParamSetFilter extends OncePerRequestFilter {
    @Autowired
    private AccountService accountService;

    @Autowired
    PersistentTokenRepository redisTokenRepository;


    private List<String> excludeMatchers;

    private Function<HttpServletRequest, Boolean> excludeMatcherFunction;

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    public ParamSetFilter() {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (excludeRequiresSetParam(request)) {
            filterChain.doFilter(request, response);
            return;
        }

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
                // 继续执行
                filterChain.doFilter(request, response);
                return;
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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

    public void setExcludeMatchers(String... excludeMatchers) {
        this.excludeMatchers = new ArrayList<>();
        this.excludeMatchers.addAll(Arrays.asList(excludeMatchers));
    }

    public void addExcludeMatchers(String... excludeMatchers) {
        if (this.excludeMatchers == null) {
            this.excludeMatchers = new ArrayList<>();
        }
        this.excludeMatchers.addAll(Arrays.asList(excludeMatchers));
    }

    public void setExcludeMatcherFunction(Function<HttpServletRequest, Boolean> excludeMatcherFunction) {
        this.excludeMatcherFunction = excludeMatcherFunction;
    }


    /**
     * 排除规则
     *
     * @param request 请求体
     * @return 是否排除
     */
    public boolean excludeRequiresSetParam(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (this.excludeMatchers != null) {
            for (String str : this.excludeMatchers) {
                if (PATH_MATCHER.match(str, uri)) {
                    return true;
                }
            }
        }

        if (this.excludeMatcherFunction != null) {
            return this.excludeMatcherFunction.apply(request);
        }

        return false;
    }
}
