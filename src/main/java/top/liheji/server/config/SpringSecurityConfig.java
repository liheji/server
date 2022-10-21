package top.liheji.server.config;

import com.alibaba.fastjson.JSONObject;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import top.liheji.server.config.auth.AuthType;
import top.liheji.server.config.auth.filter.MultipleLoginAuthenticationFilter;
import top.liheji.server.config.auth.provider.CaptchaAuthenticationProvider;
import top.liheji.server.config.filter.CaptchaFilter;
import top.liheji.server.config.filter.ParamSetFilter;
import top.liheji.server.config.auth.remember.impl.CustomTokenRememberMeServices;
import top.liheji.server.config.auth.client.MultipleAuthorizationCodeTokenResponseClient;
import top.liheji.server.config.auth.client.BaiduAuthorizationCodeTokenResponseClient;
import top.liheji.server.config.auth.client.QQAuthorizationCodeTokenResponseClient;
import top.liheji.server.config.auth.service.MultipleOAuth2UserServiceImpl;
import top.liheji.server.config.auth.service.BaiduOAuth2UserServiceImpl;
import top.liheji.server.config.auth.service.QQOAuth2UserServiceImpl;
import top.liheji.server.pojo.Account;
import top.liheji.server.util.FileUtils;
import top.liheji.server.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
    @Value("${debug}")
    private Boolean debug;

    private static final String REMEMBER_KEY = StringUtils.genUuid();

    @Autowired
    private CaptchaFilter captchaFilter;

    @Autowired
    private ParamSetFilter paramSetFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CaptchaAuthenticationProvider captchaAuthenticationProvider;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(captchaAuthenticationProvider);
        // 用户加载器
        auth.userDetailsService(userDetailsService)
                // 密码加密函数
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //验证码过滤器
        // 这些URL需要拦截并识别验证码
        captchaFilter.setMatchers("/login", "/before/forget", "/before/register", "/account/personal");
        // 设置拦截URL中需要排除验证的特殊项
        captchaFilter.setExcludeMatcherFunction(request -> {
            String uri = request.getRequestURI();

            // 手机号暂不支持验证码（没钱开）
            if (uri.startsWith("/account/personal")) {
                String property = request.getParameter("property");
                return "mobile".equals(property);
            }

            // 验证码登录不需要检查图片验证码
            if (uri.startsWith("/login")) {
                String authType = Optional.ofNullable(request.getParameter("auth-type")).orElse("").trim();
                return "CAPTCHA".equalsIgnoreCase(authType);
            }
            return false;
        });

        paramSetFilter.setExcludeMatchers("/login*", "/before/**");
        // 设置用户参数过滤器
        if (debug) {
            String[] debugArray = new String[]{"/doc.html*", "/webjars/**", "/swagger*/**", "/v2/**"};
            paramSetFilter.addExcludeMatchers(debugArray);
            http.authorizeRequests().antMatchers(debugArray).permitAll();
        }

        http.addFilterBefore(captchaFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(paramSetFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // 路径拦截设置
        http.authorizeRequests()
                // 允许直接访问路径
                .antMatchers("/before/**").permitAll()
                // 其他路径需要认证（登录）
                .anyRequest().authenticated();

        //登录设置
        http.formLogin()
                .loginPage("/login");

        // 第三方登录
        http.oauth2Login()
                .tokenEndpoint()
                .accessTokenResponseClient(accessTokenResponseClient())
                .and()
                .userInfoEndpoint()
                .userService(oauth2UserService())
                .and()
                .successHandler(oauth2LoginSuccessHandler())
                .failureHandler(oauth2LoginFailureHandler());


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

        // formLogin：不管用，需要到下方的 authenticationFilter 函数中设置
        // oauth2Login： 需要用到记住密码的服务
        http.rememberMe()
                .key(REMEMBER_KEY)
                .rememberMeServices(rememberMeServices());

        // 未登录以及登录认证设置
        http.exceptionHandling()
                .authenticationEntryPoint(exceptionHandler());

        log.info("SpringSecurity配置加载完成");
    }

    /**
     * 注入Security密码加密和比对类
     *
     * @return 加密类
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
    public CustomTokenRememberMeServices rememberMeServices() {
        return new CustomTokenRememberMeServices(REMEMBER_KEY, userDetailsService);
    }

    /**
     * Oauth 认证服务 使用code交换 access_token的具体逻辑
     *
     * @return {@link OAuth2AccessTokenResponseClient}
     */
    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        MultipleAuthorizationCodeTokenResponseClient client = new MultipleAuthorizationCodeTokenResponseClient();
        // 加入QQ自定义 QQAuthorizationCodeTokenResponseClient
        client.getMultipleClient().put(AuthType.QQ.getCode(), new QQAuthorizationCodeTokenResponseClient());
        client.getMultipleClient().put(AuthType.Baidu.getCode(), new BaiduAuthorizationCodeTokenResponseClient());
        return client;
    }

    /**
     * 请求用户信息 OAuth2User
     *
     * @return {@link OAuth2UserService}
     */
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        MultipleOAuth2UserServiceImpl service = new MultipleOAuth2UserServiceImpl();
        // 加入QQ自定义QQOAuth2UserService
        service.getMultipleService().put(AuthType.QQ.getCode(), new QQOAuth2UserServiceImpl());
        service.getMultipleService().put(AuthType.Baidu.getCode(), new BaiduOAuth2UserServiceImpl());
        return service;
    }

    /**
     * 登录成功的回调
     *
     * @return 回调函数接口
     */
    private AuthenticationSuccessHandler formLoginSuccessHandler() {
        return (req, resp, authentication) -> {
            resp.setContentType("application/json;charset=utf-8");
            Map<String, Object> objectMap = new HashMap<>(3);
            Account account = (Account) req.getAttribute("account");
            if (resp.getStatus() == HttpServletResponse.SC_NOT_ACCEPTABLE) {
                objectMap.put("code", 1);
                objectMap.put("msg", "无法识别登录设备，不允许登录");
                log.warn("无法识别的设备尝试登录");
            } else {
                objectMap.put("code", 0);
                objectMap.put("msg", "登录成功");
                objectMap.put("data", account);
            }
            PrintWriter out = resp.getWriter();
            out.write(JSONObject.toJSONString(objectMap));
            out.flush();
            out.close();
        };
    }

    /**
     * 登录失败的回调
     *
     * @return 回调函数接口
     */
    private AuthenticationFailureHandler formLoginFailureHandler() {
        return (req, resp, e) -> {
            resp.setContentType("application/json;charset=utf-8");
            PrintWriter out = resp.getWriter();
            Map<String, Object> objectMap = new HashMap<>(2);
            objectMap.put("code", 1);
            objectMap.put("msg", "认证出错，请重试。");
            out.write(JSONObject.toJSONString(objectMap));
            out.flush();
            out.close();
        };
    }

    /**
     * 登录成功的回调
     *
     * @return 回调函数接口
     */
    private AuthenticationSuccessHandler oauth2LoginSuccessHandler() {
        return (req, resp, authentication) -> {
            resp.setContentType("text/html;charset=utf-8");
            // 获取输出信息
            String info = (String) req.getAttribute("info");
            String error = (String) req.getAttribute("error");
            if (!StringUtils.isEmpty(error)) {
                resp.getWriter().write(oauth2Html(error, false));
            } else if (!StringUtils.isEmpty(info)) {
                resp.getWriter().write(oauth2Html(info, true));
            } else {
                resp.getWriter().write(oauth2Html("第三方认证失败", false));
            }
        };
    }

    /**
     * 登录失败的回调
     *
     * @return 回调函数接口
     */
    private AuthenticationFailureHandler oauth2LoginFailureHandler() {
        return (req, resp, e) -> {
            resp.setContentType("text/html;charset=utf-8");
            resp.getWriter().write(oauth2Html("第三方认证失败", false));
        };
    }

    /**
     * 登出成功的回调
     *
     * @return 回调函数接口
     */
    private LogoutSuccessHandler logoutSuccessHandler() {
        return (req, resp, authentication) -> {
            resp.setContentType("application/json;charset=utf-8");
            PrintWriter out = resp.getWriter();
            Map<String, Object> objectMap = new HashMap<>(2);
            objectMap.put("code", 0);
            objectMap.put("msg", "注销成功");
            out.write(JSONObject.toJSONString(objectMap));
            out.flush();
            out.close();
        };
    }

    /**
     * 登录异常的回调
     *
     * @return 回调函数接口
     */
    private AuthenticationEntryPoint exceptionHandler() {
        return (req, resp, authException) -> {
            String tid = req.getParameter("token");
            if (tid == null || "".equals(tid.trim())) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        };
    }


    /**
     * 拼接返回的HTML
     *
     * @param msg    信息
     * @param toMain 是否去主页
     * @return HTML文本
     */
    private String oauth2Html(String msg, boolean toMain) {
        final String path = toMain ? "/#/main/personal" : "/#/login?msg=" + msg;
        StringBuilder builder = new StringBuilder();
        try {
            File oauth2 = FileUtils.resourceFile("templates", "oauth2.html");
            @Cleanup FileInputStream fis = new FileInputStream(oauth2);
            @Cleanup InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            @Cleanup BufferedReader reader = new BufferedReader(isr);
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (Exception ignored) {
        }
        return String.format(builder.toString(), path);
    }
}
