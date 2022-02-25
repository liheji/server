package top.liheji.server.config.filter;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.PassToken;
import top.liheji.server.pojo.PersistentLogins;
import top.liheji.server.service.AccountService;
import top.liheji.server.service.PassTokenService;
import top.liheji.server.service.PersistentLoginsService;


import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Time : 2022/1/30 8:59
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : serverPlus
 * @Description :
 */
@Component
public class ParamSetFilter extends OncePerRequestFilter {

    private String tokenName = "token";

    private String cookieName = "sessionid";

    private String[] matchers;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PassTokenService passTokenService;

    @Autowired
    private PersistentLoginsService persistentLoginsService;

    public ParamSetFilter() {
        setMatchers("/login", "/before");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (!requiresSetParam(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        //设置参数
        String token = request.getParameter(this.tokenName);
        PassToken passToken = passTokenService.selectTokenByKey(token);
        if (passToken != null) {
            passToken.getAccount().setPassword("");
            request.setAttribute("account", passToken.getAccount());

            filterChain.doFilter(request, response);
            return;
        }

        String cookieValue = extractRememberMeCookie(request, this.cookieName);
        if (cookieValue != null) {
            String[] strings = decodeCookie(cookieValue);
            if (strings != null) {
                PersistentLogins logins = persistentLoginsService.getOne(new QueryWrapper<PersistentLogins>().eq("series", strings[0]));
                if (logins == null) {
                    request.getSession().invalidate();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                Account account = accountService.getOne(
                        new QueryWrapper<Account>()
                                .eq("username", logins.getUsername())
                );
                account.setPassword("");

                request.setAttribute("series", strings[0]);
                request.setAttribute("account", account);

                filterChain.doFilter(request, response);
                return;
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

//        if (request.getRequestURI().startsWith("/status")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        response.setContentType("application/json;charset=utf-8");
//        PrintWriter out = response.getWriter();
//        Map<String, Object> objectMap = new HashMap<>(2);
//        objectMap.put("code", 1);
//        objectMap.put("msg", "非法请求");
//        out.write(JSONObject.toJSONString(objectMap));
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

    private String extractRememberMeCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if ((cookies == null) || (cookies.length == 0)) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public void setMatchers(String... matchers) {
        this.matchers = matchers;
    }

    public boolean requiresSetParam(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (this.matchers != null) {
            for (String str : this.matchers) {
                if (uri.startsWith(str)) {
                    return false;
                }
            }
        }

        return true;
    }
}
