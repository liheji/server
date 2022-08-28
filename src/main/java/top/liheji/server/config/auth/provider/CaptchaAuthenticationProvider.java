package top.liheji.server.config.auth.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import top.liheji.server.config.auth.CaptchaAuthenticationToken;
import top.liheji.server.service.CaptchaService;

import java.util.Objects;

/**
 * @author : Galaxy
 * @time : 2022/8/20 9:26
 * @create : IdeaJ
 * @project : server
 * @description : 验证码登录
 */
@Slf4j
@Component
public class CaptchaAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private UserDetailsService userDetailsService;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (Objects.isNull(userDetails)) {
            throw new BadCredentialsException("当前账号不存在");
        }

        if (!captchaService.checkCaptcha(password, username)) {
            throw new BadCredentialsException("验证码错误");
        }

        CaptchaAuthenticationToken result = new CaptchaAuthenticationToken(
                userDetails,
                authentication.getCredentials(),
                this.authoritiesMapper.mapAuthorities(userDetails.getAuthorities())
        );
        result.setDetails(authentication.getDetails());
        return result;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return CaptchaAuthenticationToken.class.isAssignableFrom(aClass);
    }

    public GrantedAuthoritiesMapper getAuthoritiesMapper() {
        return authoritiesMapper;
    }

    public void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper) {
        this.authoritiesMapper = authoritiesMapper;
    }
}
