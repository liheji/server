package top.yilee.server.vo;

import lombok.Data;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import top.yilee.server.constant.ServerConstant;
import top.yilee.server.pojo.Account;
import top.yilee.server.service.CaptchaService;
import top.yilee.server.util.BeanUtils;
import top.yilee.server.util.SecurityUtils;

/**
 * @author : Galaxy
 * @time : 2023/2/13 21:32
 * @create : IdeaJ
 * @project : server
 * @description :
 */
@Data
public class PersonalVo {
    private String property;
    private String value;
    private String captcha;
    private String newPassword;


    /**
     * 安全检查，负责检查用户更新的数据是否合法
     *
     * @throws RuntimeException 未通过检查
     */
    public void securityCheck() throws RuntimeException {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        CaptchaService captchaService = BeanUtils.getBean(CaptchaService.class);
        switch (property) {
            case "password":
                //重新查询密码h
                if (!SecurityUtils.match(current.getPassword(), value)) {
                    throw new BadCredentialsException("密码错误");
                }
                value = SecurityUtils.bcrypt(newPassword);
            case "email":
            case "mobile":
                if (!captchaService.checkCaptcha(current.getUsername(), captcha)) {
                    throw new DisabledException("验证码错误");
                }
                break;
            default:
                throw new IllegalArgumentException("参数错误");
        }
    }
}
