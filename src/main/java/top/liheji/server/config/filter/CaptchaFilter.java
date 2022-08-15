package top.liheji.server.config.filter;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
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
import java.util.function.Function;

/**
 * @author : Galaxy
 * @time : 2022/1/29 20:47
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 借用用户名密码的过滤器实现验证码功能
 */
@Component
public class CaptchaFilter extends OncePerRequestFilter {

    @Autowired
    private CaptchaService captchaService;

    private String captchaParameter = "captcha";

    private String[] matchers;

    private Function<HttpServletRequest, Boolean> otherMatcherFunction;

    public CaptchaFilter() {
    }

    public void setMatchers(String... matchers) {
        this.matchers = matchers;
    }

    public void setCaptchaParameter(String captchaParameter) {
        this.captchaParameter = captchaParameter;
    }

    public void setOtherMatcherFunction(Function<HttpServletRequest, Boolean> otherMatcherFunction) {
        this.otherMatcherFunction = otherMatcherFunction;
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
            out.close();
        }
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        String method = request.getMethod().toLowerCase();
        return "post".equals(method) || "put".equals(method) || "delete".equals(method);
    }

    private void attemptAuthentication(HttpServletRequest request) throws AuthenticationException {
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

    @Nullable
    protected String obtainCaptcha(HttpServletRequest request) {
        return request.getParameter(this.captchaParameter);
    }

    private boolean requiresCaptcha(HttpServletRequest request) {
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

    private boolean otherRequireCaptcha(HttpServletRequest request) {
        if (this.otherMatcherFunction != null) {
            return this.otherMatcherFunction.apply(request);
        }
        return false;
    }
}
