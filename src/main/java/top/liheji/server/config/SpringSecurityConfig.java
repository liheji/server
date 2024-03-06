package top.liheji.server.config;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import top.dcenter.ums.security.core.oauth.config.Auth2AutoConfigurer;
import top.dcenter.ums.security.core.oauth.properties.Auth2Properties;
import top.liheji.server.config.auth.constant.AuthConstant;
import top.liheji.server.config.auth.remember.RedisTokenBasedRememberMeServices;
import top.liheji.server.config.filter.SetComDataFilter;
import top.liheji.server.config.filter.AuthFilter;
import top.liheji.server.config.auth.filter.MultipleLoginAuthenticationFilter;
import top.liheji.server.config.auth.provider.CaptchaAuthenticationProvider;
import top.liheji.server.config.filter.CaptchaFilter;
import top.liheji.server.constant.ServerConstant;
import top.liheji.server.pojo.Account;
import top.liheji.server.util.R;
import top.liheji.server.util.WebUtils;

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
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${debug: false}")
    private Boolean debug;

    @Autowired
    private CaptchaFilter captchaFilter;

    @Autowired
    private SetComDataFilter setComDataFilter;

    @Autowired
    private AuthFilter authFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CaptchaAuthenticationProvider captchaAuthenticationProvider;

    @Autowired
    private PersistentTokenRepository redisTokenRepository;

    @Autowired
    private Auth2AutoConfigurer auth2AutoConfigurer;

    @Autowired
    private Auth2Properties auth2Properties;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(captchaAuthenticationProvider);
        // 用户加载器
        auth.userDetailsService(userDetailsService)
                // 密码加密函数
                .passwordEncoder(new BCryptPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //验证码过滤器
        // 这些 URL需要拦截并识别验证码
        captchaFilter.setMatchers("/login", "/before/forget", "/before/register");
        // 设置拦截URL中需要排除验证的特殊项
        authFilter.setExcludeMatchers("/login*", "/before/**");
        // 设置用户参数过滤器
        if (debug) {
            String[] debugArray = new String[]{"/doc.html*", "/webjars/**", "/swagger*/**", "/v2/**"};
            authFilter.addExcludeMatchers(debugArray);
            http.authorizeRequests().antMatchers(debugArray).permitAll();
        }

        http.addFilterBefore(setComDataFilter, LogoutFilter.class)
                .addFilterBefore(captchaFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(authFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(authenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // 路径拦截设置
        http.authorizeRequests()
                // 允许直接访问路径
                .antMatchers("/before/**")
                .permitAll();

        //登录设置
        http.formLogin()
                .loginPage("/login");

        // 第三方登录
        http.apply(this.auth2AutoConfigurer);
        http.authorizeRequests()
                .antMatchers(HttpMethod.GET,
                        auth2Properties.getRedirectUrlPrefix() + "/*",
                        auth2Properties.getAuthLoginUrlPrefix() + "/*")
                .permitAll();

        http.authorizeRequests()
                .anyRequest().authenticated();

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
//                .disable();
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringAntMatchers("/before/cdn");

        http.rememberMe()
                .key(AuthConstant.REMEMBER_ME_KEY)
                .rememberMeServices(rememberMeServices());

        // 未登录以及登录认证设置
        http.exceptionHandling()
                .authenticationEntryPoint(exceptionHandler());

        log.info("SpringSecurity配置加载完成");
    }

    /**
     * 聚合登录过滤器
     *
     * @return 登录过滤器
     * @throws Exception 异常
     */
    @Bean
    public MultipleLoginAuthenticationFilter authenticationFilter() throws Exception {
        MultipleLoginAuthenticationFilter filter = new MultipleLoginAuthenticationFilter(super.authenticationManager());
        // 记住密码设置
        filter.setRememberMeServices(rememberMeServices());
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
    public RedisTokenBasedRememberMeServices rememberMeServices() {
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
            R r = R.ok().put("data", Base64.encode(JSON.toJSONBytes(current)));
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
