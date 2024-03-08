package top.liheji.server.config.auth.filter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import top.liheji.server.config.auth.CaptchaAuthenticationToken;
import top.liheji.server.config.auth.constant.AuthType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * @author : Galaxy
 * @time : 2022/8/20 13:11
 * @create : IdeaJ
 * @project : server
 * @description : 重写 UsernamePasswordAuthenticationFilter，实现多种情况的登录
 */
@Getter
@Setter
public class MultipleLoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private String authTypeParameter = "auth-type";

    public MultipleLoginAuthenticationFilter() {
        super();
    }

    public MultipleLoginAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        AuthType authType = AuthType.getByCode(obtainAuthType(request));
        String username = obtainUsername(request);
        String password = obtainPassword(request);
        AbstractAuthenticationToken authRequest;
        if (authType == AuthType.PASSWORD) {
            // 用户名密码登录
            authRequest = new UsernamePasswordAuthenticationToken(username, password);
        } else {
            // 用户验证码登录（手机号或邮箱）
            authRequest = new CaptchaAuthenticationToken(username, password);
        }
        // Allow subclasses to set the "details" property
        setCurDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    protected void setCurDetails(HttpServletRequest request, AbstractAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

    @Override
    protected String obtainUsername(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(SPRING_SECURITY_FORM_USERNAME_KEY)).orElse("").trim();
    }

    @Override
    protected String obtainPassword(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(SPRING_SECURITY_FORM_PASSWORD_KEY)).orElse("").trim();
    }

    /**
     * 获取登录方法
     *
     * @param request so that request attributes can be retrieved
     * @return the username that will be presented in the <code>Authentication</code>
     */
    protected String obtainAuthType(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(this.authTypeParameter)).orElse("").trim();
    }
}
