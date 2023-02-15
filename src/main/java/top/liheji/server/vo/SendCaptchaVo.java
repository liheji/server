package top.liheji.server.vo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import org.springframework.util.ObjectUtils;
import top.liheji.server.constant.CaptchaTypeEnum;
import top.liheji.server.constant.ServerConstant;
import top.liheji.server.pojo.Account;
import top.liheji.server.service.AccountService;
import top.liheji.server.util.BeanUtils;

/**
 * @author : Galaxy
 * @time : 2023/2/15 15:43
 * @create : IdeaJ
 * @project : server
 * @description :
 */
@Data
public class SendCaptchaVo {
    private String receiver;
    private String property;

    /**
     * 安全检查，负责检查数据是否合理
     *
     * @throws RuntimeException 未通过检查
     */
    public CaptchaTypeEnum afterSecurityCheck() throws RuntimeException {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        CaptchaTypeEnum captchaType = CaptchaTypeEnum.from(property);

        if (ObjectUtils.isEmpty(current)
                || ObjectUtils.isEmpty(captchaType)
                || ObjectUtils.isEmpty(receiver)) {
            throw new IllegalArgumentException("参数错误，请检查");
        }
        switch (captchaType) {
            case EMAIL: {
                if (ObjectUtils.isEmpty(current.getEmail()) && !receiver.equals(current.getEmail())) {
                    throw new RuntimeException("验证失败");
                }
            }
            break;
            case MOBILE: {
                if (!ObjectUtils.isEmpty(current.getMobile()) && !receiver.equals(current.getMobile())) {
                    throw new RuntimeException("验证失败");
                }
                throw new UnsupportedOperationException("暂不支持手机号验证");
            }
            default:
                throw new IllegalArgumentException("参数错误，请检查");
        }
        return captchaType;
    }

    /**
     * 安全检查，负责检查数据是否合理
     *
     * @throws RuntimeException 未通过检查
     */
    public CaptchaTypeEnum beforeSecurityCheck() throws RuntimeException {
        AccountService accountService = BeanUtils.getBean(AccountService.class);
        Account current = accountService.getOne(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getUsername, receiver)
                        .or()
                        .eq(Account::getEmail, receiver)
                        .or()
                        .eq(Account::getMobile, receiver)
        );
        CaptchaTypeEnum captchaType = CaptchaTypeEnum.from(property);

        if (ObjectUtils.isEmpty(current)
                || ObjectUtils.isEmpty(captchaType)
                || ObjectUtils.isEmpty(receiver)) {
            throw new IllegalArgumentException("参数错误，请检查");
        }

        switch (captchaType) {
            case EMAIL:
                break;
            case MOBILE: {
                throw new UnsupportedOperationException("暂不支持手机号验证");
            }
            default:
                throw new IllegalArgumentException("参数错误，请检查");
        }
        return captchaType;
    }
}
