package top.liheji.server.config.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @Time : 2022/1/29 21:11
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : serverPlus
 * @Description :
 */
public class CaptchaException extends AuthenticationException {
    public CaptchaException(String msg) {
        super(msg);
    }
}
