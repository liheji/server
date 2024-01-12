package top.liheji.server.config.filter;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import top.liheji.server.constant.ServerConstant;
import top.liheji.server.pojo.Account;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author : Galaxy
 * @time : 2022/1/30 8:59
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 系统登录后自动设置系统用户到共享数据
 */
@Component
public class AuthFilter extends OncePerRequestFilter {
    private List<String> excludeMatchers;

    private Function<HttpServletRequest, Boolean> excludeMatcherFunction;

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    public AuthFilter() {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (excludeRequiresSetParam(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Account obj = ServerConstant.LOCAL_ACCOUNT.get();
        if (obj != null) {
            // 继续执行
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    public void setExcludeMatchers(String... excludeMatchers) {
        this.excludeMatchers = new ArrayList<>();
        this.excludeMatchers.addAll(Arrays.asList(excludeMatchers));
    }

    public void addExcludeMatchers(String... excludeMatchers) {
        if (this.excludeMatchers == null) {
            this.excludeMatchers = new ArrayList<>();
        }
        this.excludeMatchers.addAll(Arrays.asList(excludeMatchers));
    }

    public void setExcludeMatcherFunction(Function<HttpServletRequest, Boolean> excludeMatcherFunction) {
        this.excludeMatcherFunction = excludeMatcherFunction;
    }


    /**
     * 排除规则
     *
     * @param request 请求体
     * @return 是否排除
     */
    public boolean excludeRequiresSetParam(HttpServletRequest request) {
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
