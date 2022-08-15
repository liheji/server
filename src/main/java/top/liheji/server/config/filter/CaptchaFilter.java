package top.liheji.server.config.filter;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import top.liheji.server.service.CaptchaService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : Galaxy
 * @time : 2022/1/29 20:47
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 借用用户名密码的过滤器实现验证码功能
 */
@Slf4j
@Component
public class CaptchaFilter extends OncePerRequestFilter {
    @Autowired
    private CaptchaService captchaService;

    private String captchaParameter = "captcha";

    private String[] matchers;

    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/login", "POST");

    public CaptchaFilter() {
        setMatchers("/login", "/before/forget", "/before/register");
    }

    public String getCaptchaParameter() {
        return captchaParameter;
    }

    public void setCaptchaParameter(String captchaParameter) {
        this.captchaParameter = captchaParameter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!requiresAuthentication(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            this.attemptAuthentication(request);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            response.setContentType("application/json;charset=utf-8");
            PrintWriter out = response.getWriter();
            Map<String, Object> objectMap = new HashMap<>(2);
            objectMap.put("code", 1);
            objectMap.put("msg", ex.getMessage());
            out.write(JSONObject.toJSONString(objectMap));
        }
    }

    public void attemptAuthentication(HttpServletRequest request) throws AuthenticationException {
        String method = request.getMethod();
        if (!"post".equalsIgnoreCase(method) &&
                !"put".equalsIgnoreCase(method) &&
                !"delete".equalsIgnoreCase(method)) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        if (requiresCaptcha(request) || otherRequireCaptcha(request)) {
            String captcha = obtainCaptcha(request);

            if (captcha == null) {
                throw new DisabledException("验证码不能为空");
            }

            if (!captchaService.checkCaptcha(captcha)) {
                throw new DisabledException("验证码错误");
            }
        }
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        return DEFAULT_ANT_PATH_REQUEST_MATCHER.matches(request);
    }

    @Nullable
    protected String obtainCaptcha(HttpServletRequest request) {
        return request.getParameter(this.captchaParameter);
    }

    public void setMatchers(String... matchers) {
        this.matchers = matchers;
    }

    public boolean requiresCaptcha(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (this.matchers != null) {
            for (String str : this.matchers) {
                if (uri.startsWith(str)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean otherRequireCaptcha(HttpServletRequest request) {
        String property = request.getParameter("property");
        return "email".equals(property) || "password".equals(property);
    }
}
