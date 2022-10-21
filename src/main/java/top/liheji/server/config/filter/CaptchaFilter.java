package top.liheji.server.config.filter;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import top.liheji.server.service.CaptchaService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Function;

/**
 * @author : Galaxy
 * @time : 2022/1/29 20:47
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 过滤器: 实现验证码自主验证功能
 */
@Component
public class CaptchaFilter extends OncePerRequestFilter {

    @Autowired
    private CaptchaService captchaService;

    private String captchaParameter = "captcha";

    private List<String> matchers;

    private List<String> excludeMatchers;

    private Function<HttpServletRequest, Boolean> otherMatcherFunction;

    private Function<HttpServletRequest, Boolean> excludeMatcherFunction;

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    public CaptchaFilter() {
    }

    public void setMatchers(String... matchers) {
        this.matchers = new ArrayList<>();
        this.matchers.addAll(Arrays.asList(matchers));
    }

    public void addMatchers(String... matchers) {
        if (this.matchers == null) {
            this.matchers = new ArrayList<>(matchers.length);
        }
        this.matchers.addAll(Arrays.asList(matchers));
    }

    public void setExcludeMatchers(String... excludeMatchers) {
        this.excludeMatchers = new ArrayList<>();
        this.excludeMatchers.addAll(Arrays.asList(excludeMatchers));
    }

    public void addExcludeMatchers(String... excludeMatchers) {
        if (this.excludeMatchers == null) {
            this.excludeMatchers = new ArrayList<>(excludeMatchers.length);
        }
        this.excludeMatchers.addAll(Arrays.asList(excludeMatchers));
    }

    public void setCaptchaParameter(String captchaParameter) {
        this.captchaParameter = captchaParameter;
    }

    public void setOtherMatcherFunction(Function<HttpServletRequest, Boolean> otherMatcherFunction) {
        this.otherMatcherFunction = otherMatcherFunction;
    }

    public void setExcludeMatcherFunction(Function<HttpServletRequest, Boolean> excludeMatcherFunction) {
        this.excludeMatcherFunction = excludeMatcherFunction;
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
            out.flush();
            out.close();
        }
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        String method = request.getMethod().toLowerCase();
        return "post".equals(method) || "put".equals(method) || "delete".equals(method);
    }

    private void attemptAuthentication(HttpServletRequest request) throws AuthenticationException {
        if (requiresCaptcha(request) && !excludeRequireCaptcha(request)) {
            String captcha = obtainCaptcha(request);

            if (captcha == null) {
                throw new DisabledException("验证码不能为空");
            }

            if (!captchaService.checkCaptcha(captcha)) {
                throw new DisabledException("验证码错误");
            }
        }
    }

    @Nullable
    protected String obtainCaptcha(HttpServletRequest request) {
        return request.getParameter(this.captchaParameter);
    }

    /**
     * 匹配规则（函数相对排除规则滞后）
     *
     * @param request 请求体
     * @return 是否匹配
     */
    private boolean requiresCaptcha(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (this.matchers != null) {
            for (String str : this.matchers) {
                if (PATH_MATCHER.match(str, uri)) {
                    return true;
                }
            }
        }
        if (this.otherMatcherFunction != null) {
            return this.otherMatcherFunction.apply(request);
        }
        return false;
    }

    /**
     * 排除规则（函数相对匹配规则优先）
     *
     * @param request 请求体
     * @return 是否排除
     */
    private boolean excludeRequireCaptcha(HttpServletRequest request) {
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
