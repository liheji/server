package top.yilee.server.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author : Galaxy
 * @time : 2023/2/13 22:20
 * @create : IdeaJ
 * @project : server
 * @description :
 */
public class SecurityUtils {
    /**
     * 密码加密
     */
    public static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 校验密码
     *
     * @param old 旧密码
     * @param pwd 输入的密码
     * @return 是否核验成功
     */
    public static boolean match(String old, String pwd) {
        return old != null && pwd != null && ENCODER.matches(pwd, old);
    }

    /**
     * 将明文的密码加密
     *
     * @param pwd 密码
     * @return 加密后的密码
     */
    public static String bcrypt(String pwd) {
        return ENCODER.encode(pwd);
    }

    /**
     * 鉴权
     *
     * @param authority 需要的权限
     * @return 是否有权限
     */
    public static boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (auth.getAuthority().equals(authority)) {
                return true;
            }
        }
        return false;
    }
}
