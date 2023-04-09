package top.liheji.server.config.filter;

import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import top.liheji.server.constant.ServerConstant;
import top.liheji.server.pojo.AuthDevices;
import top.liheji.server.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author : Galaxy
 * @time : 2022/1/30 8:59
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 系统登录前自动设置 UserAgent到共享数据
 */
@Component
public class SetComDataFilter extends OncePerRequestFilter {
    public SetComDataFilter() {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 验证登录设备信息
        AuthDevices device = WebUtils.parseAgent(request);
        if (ObjectUtils.isEmpty(device)) {
            throw new DisabledException("无法识别的访问设备");
        }
        ServerConstant.LOCAL_DEVICE.set(device);
        filterChain.doFilter(request, response);
    }
}
