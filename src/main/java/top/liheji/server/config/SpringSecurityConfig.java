package top.liheji.server.config;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import top.liheji.server.config.filter.CaptchaVerifyFilter;
import top.liheji.server.config.filter.ParamSetFilter;
import top.liheji.server.config.remember.CustomPersistentTokenRepository;
import top.liheji.server.config.remember.impl.CustomJdbcTokenRepositoryImpl;
import top.liheji.server.config.remember.impl.CustomTokenRememberMeServices;
import top.liheji.server.pojo.Account;
import top.liheji.server.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @Time : 2022/1/24 13:21
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : mybatis-gen
 * @Description :
 */
@Slf4j
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    private final String rememberKey = StringUtils.genUuid();

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ParamSetFilter paramSetFilter;

    @Autowired
    private CaptchaVerifyFilter captchaVerifyFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 用户加载器
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //验证码过滤器
        http.addFilterBefore(captchaVerifyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(paramSetFilter, UsernamePasswordAuthenticationFilter.class);

        // 路径拦截设置
        http.authorizeRequests()
                .antMatchers("/login", "/before/**")
                .permitAll()
                .anyRequest()
                .authenticated();

        //登录设置
        http.formLogin()
                .loginPage("/login")
                .failureHandler((req, resp, e) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    Map<String, Object> objectMap = new HashMap<>(2);
                    objectMap.put("code", 1);

                    String msg = e.getMessage();
                    if (e instanceof LockedException) {
                        msg = "账户被锁定";
                    } else if (e instanceof CredentialsExpiredException) {
                        msg = "密码过期";
                    } else if (e instanceof AccountExpiredException) {
                        msg = "账户过期";
                    } else if (e instanceof DisabledException) {
                        msg = "账户被禁用";
                    } else if (e instanceof BadCredentialsException) {
                        msg = "用户名或密码错误";
                    }

                    objectMap.put("msg", msg);
                    out.write(JSONObject.toJSONString(objectMap));
                    out.flush();
                    out.close();
                })
                .successHandler((req, resp, authentication) -> {
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
                })
                .permitAll();

        // 登出设置
        http.logout()
                .logoutUrl("/logout")
                .deleteCookies()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutSuccessHandler(((req, resp, authentication) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    Map<String, Object> objectMap = new HashMap<>(2);
                    objectMap.put("code", 0);
                    objectMap.put("msg", "注销成功");
                    out.write(JSONObject.toJSONString(objectMap));
                    out.flush();
                    out.close();
                }))
                .permitAll();

        // 记住密码设置
        http.rememberMe()
                .key(rememberKey)
                .rememberMeServices(rememberMeServices());

        // 未登录以及登录认证设置
        http.csrf()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint((req, resp, authException) -> {
                    String tid = req.getParameter("token");
                    if (tid == null || "".equals(tid.trim())) {
                        resp.setContentType("application/json;charset=utf-8");
                        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                });

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
     * 注入Security的session数据库表
     *
     * @return 数据库操作类
     */
    @Bean
    public CustomPersistentTokenRepository persistentTokenRepository() {
        CustomJdbcTokenRepositoryImpl jdbcToken = new CustomJdbcTokenRepositoryImpl();
        jdbcToken.setDataSource(dataSource);
        return jdbcToken;
    }

    /**
     * 注入Security的rememberMeServices数据库表
     *
     * @return 数据库服务类
     */
    @Bean
    public CustomTokenRememberMeServices rememberMeServices() {
        return new CustomTokenRememberMeServices(rememberKey, userDetailsService, persistentTokenRepository());
    }
}
