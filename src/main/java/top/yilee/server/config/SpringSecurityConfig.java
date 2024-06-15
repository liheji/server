package top.yilee.server.config;

import cn.hutool.core.codec.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import top.dcenter.ums.security.core.oauth.config.Auth2AutoConfigurer;
import top.dcenter.ums.security.core.oauth.properties.Auth2Properties;
import top.yilee.server.config.auth.constant.AuthConstant;
import top.yilee.server.config.auth.filter.MultipleLoginAuthenticationFilter;
import top.yilee.server.config.auth.provider.CaptchaAuthenticationProvider;
import top.yilee.server.config.auth.remember.RedisTokenBasedRememberMeServices;
import top.yilee.server.config.filter.AuthFilter;
import top.yilee.server.config.filter.CaptchaFilter;
import top.yilee.server.config.filter.SetComDataFilter;
import top.yilee.server.constant.ServerConstant;
import top.yilee.server.pojo.Account;
import top.yilee.server.util.JsonUtils;
import top.yilee.server.util.R;
import top.yilee.server.util.WebUtils;

/**
 * @author : Galaxy
 * @time : 2022/1/24 13:21
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 设置 SpringSecurity 相关配置
 * 加载过滤器
 * 设置放行URL
 * 设置登录成功和登录失败回调
 * 设置登出回调
 * 设置记住密码功能
 * 设置未登录访问请求拦截
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SpringSecurityConfig {
    @Value("${debug: false}")
    private Boolean debug;

    @Autowired
    private CaptchaFilter captchaFilter;

    @Autowired
    private SetComDataFilter setComDataFilter;

    @Autowired
    private AuthFilter authFilter;

    @Autowired
    @Lazy
    private Auth2Properties auth2Properties;

    @Autowired
    @Lazy
    private Auth2AutoConfigurer auth2AutoConfigurer;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   MultipleLoginAuthenticationFilter multipleLoginAuthenticationFilter,
                                                   RedisTokenBasedRememberMeServices redisTokenBasedRememberMeServices) throws Exception {
        //验证码过滤器
        // 这些 URL需要拦截并识别验证码
        captchaFilter.setMatchers("/login", "/before/forget", "/before/register");
        // 设置拦截URL中需要排除验证的特殊项
        authFilter.setExcludeMatchers("/login*", "/before/**");

        // 过滤器位置
        http.addFilterBefore(setComDataFilter, LogoutFilter.class)
                .addFilterBefore(captchaFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(authFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(multipleLoginAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 路径拦截设置
        http.authorizeRequests()
                .antMatchers("/before/**").permitAll();

        //登录设置
        http.formLogin().disable(); // 禁用 UsernamePasswordAuthenticationFilter
        http.oauth2Login().disable();  // 禁用 OAuth2LoginAuthenticationFilter

        // 第三方登录
        http.apply(this.auth2AutoConfigurer);
        http.authorizeRequests()
                .antMatchers(HttpMethod.GET,
                        auth2Properties.getRedirectUrlPrefix() + "/*",
                        auth2Properties.getAuthLoginUrlPrefix() + "/*")
                .permitAll();

        http.authorizeRequests()
                .anyRequest()
                .authenticated();

        // 登出设置
        http.logout()
                .logoutUrl("/logout")
                .deleteCookies()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutSuccessHandler(logoutSuccessHandler())
                .permitAll();

        // 跨域攻击拦截
        http.csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringAntMatchers("/before/cdn");

        if (debug) {
            http.csrf().disable();
        }

        // 记住密码
        http.rememberMe()
                .key(AuthConstant.REMEMBER_ME_KEY)
                .rememberMeServices(redisTokenBasedRememberMeServices)
                .authenticationSuccessHandler(formLoginSuccessHandler());

        // 未登录以及登录认证设置
        http.exceptionHandling()
                .authenticationEntryPoint(exceptionHandler());

        log.info("SpringSecurity配置加载完成");

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                       UserDetailsService userDetailsService,
                                                       BCryptPasswordEncoder bCryptPasswordEncoder,
                                                       CaptchaAuthenticationProvider captchaAuthenticationProvider) throws Exception {

        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.authenticationProvider(captchaAuthenticationProvider)
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder);

        return auth.build();
    }

    /**
     * 密码加密方式
     *
     * @return 密码加密方式
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 聚合登录过滤器
     *
     * @return 登录过滤器
     */
    @Bean
    public MultipleLoginAuthenticationFilter multipleLoginAuthenticationFilter(AuthenticationManager authenticationManager,
                                                                               RedisTokenBasedRememberMeServices redisTokenBasedRememberMeServices) {

        MultipleLoginAuthenticationFilter filter = new MultipleLoginAuthenticationFilter(authenticationManager);
        // 记住密码设置
        filter.setRememberMeServices(redisTokenBasedRememberMeServices);
        // 登录成功和失败的回调
        filter.setAuthenticationSuccessHandler(formLoginSuccessHandler());
        filter.setAuthenticationFailureHandler(formLoginFailureHandler());

        return filter;
    }

    /**
     * 记住密码服务
     *
     * @return 记住密码服务
     */
    @Bean
    public RedisTokenBasedRememberMeServices redisTokenBasedRememberMeServices(UserDetailsService userDetailsService,
                                                                               PersistentTokenRepository redisTokenRepository) {

        RedisTokenBasedRememberMeServices rememberMeServices = new RedisTokenBasedRememberMeServices(
                AuthConstant.REMEMBER_ME_KEY,
                userDetailsService,
                redisTokenRepository);

        // 设置记住密码相关设置
        rememberMeServices.setAlwaysRemember(true);
        rememberMeServices.setCookieName(AuthConstant.REMEMBER_COOKIE_NAME);
        rememberMeServices.setParameter(AuthConstant.REMEMBER_PARAMETER);
        rememberMeServices.setTokenValiditySeconds((int) AuthConstant.REMEMBER_EXPIRE_SECONDS);

        return rememberMeServices;
    }

    /**
     * 登录成功的回调
     *
     * @return 回调函数接口
     */
    private AuthenticationSuccessHandler formLoginSuccessHandler() {
        return (req, resp, authentication) -> {
            Account current = ServerConstant.LOCAL_ACCOUNT.get();
            current.setPassword("");
            R r = R.ok().put("data", Base64.encode(JsonUtils.toJSONBytes(current)));
            WebUtils.response(resp, r);
        };
    }

    /**
     * 登录失败的回调
     *
     * @return 回调函数接口
     */
    private AuthenticationFailureHandler formLoginFailureHandler() {
        return (req, resp, e) -> {
            e.printStackTrace();
            WebUtils.response(resp, R.error(e.getMessage()));
        };
    }

    /**
     * 登出成功的回调
     *
     * @return 回调函数接口
     */
    private LogoutSuccessHandler logoutSuccessHandler() {
        return (req, resp, authentication) -> {
            WebUtils.response(resp, R.ok());
        };
    }

    /**
     * 登录异常的回调
     *
     * @return 回调函数接口
     */
    private AuthenticationEntryPoint exceptionHandler() {
        return (req, resp, authException) -> {
            authException.printStackTrace();
            WebUtils.response(resp, R.error(authException.getMessage()));
        };
    }
}
