package top.liheji.server.service.impl;

import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.CaptchaAbstract;
import com.wf.captcha.base.Randoms;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import top.liheji.server.service.CaptchaService;
import top.liheji.server.constant.CaptchaTypeEnum;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author : Galaxy
 * @time : 2022/1/17 21:28
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 验证码生成服务的具体实现
 */
@Slf4j
@Service("captchaService")
public class CaptchaServiceImpl implements CaptchaService {
    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String REDIS_CAPTCHA_PREFIX = "server:captcha:";

    @Override
    public String genCaptcha(String username, CaptchaTypeEnum captchaType) {
        CaptchaAbstract captcha = getCaptchaService(username, captchaType);
        return captcha.text();
    }

    @Override
    public String genImgCaptcha(String username, CaptchaTypeEnum captchaType) {
        CaptchaAbstract captcha = getCaptchaService(username, captchaType);
        return captcha.toBase64();
    }

    @Override
    public void sendCaptcha(String username, String receiver, CaptchaTypeEnum captchaType) {
        CaptchaAbstract captcha = new SpecCaptcha();
        saveCaptcha(captcha, username, captchaType);
        switch (captchaType) {
            case EMAIL: {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setSubject("SERVER 邮箱验证");
                message.setFrom("2820054672@qq.com");
                message.setTo(receiver);
                message.setSentDate(new Date());
                message.setText(String.format("你申请的验证码为：%s\n10分钟内有效\n请尽快输入你的验证码\n", captcha.text()));
                javaMailSender.send(message);
            }
            break;
            case MOBILE: {
                log.info(String.format("你申请的验证码为：%s\n10分钟内有效\n请尽快输入你的验证码\n", captcha.text()));
                // TODO 发送手机验证码
            }
            break;
            default:
                break;
        }
    }

    @Override
    public boolean checkCaptcha(String username, String code) {
        if (code == null) {
            return false;
        }

        if (username == null || username.trim().isEmpty()) {
            username = "";
        }

        String key = String.format("%s%s:%s", REDIS_CAPTCHA_PREFIX, username, code.toLowerCase());
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }

    /**
     * 获取验证码服务
     *
     * @param username    用户名
     * @param captchaType 验证码类型
     * @return 验证码服务
     */
    private CaptchaAbstract getCaptchaService(String username, CaptchaTypeEnum captchaType) {
        CaptchaAbstract captcha;
        switch (captchaType) {
            case GENERAL: {
                switch (Randoms.num(3)) {
                    case 1:
                        captcha = new ArithmeticCaptcha(100, 38, 2);
                        break;
                    case 2:
                        captcha = new GifCaptcha(100, 38, 4);
                        break;
                    default:
                        captcha = new SpecCaptcha(100, 38, 4);
                        break;
                }
            }
            break;
            case GENERAL_SECRET:
            case REGISTER_SECRET: {
                captcha = new SpecCaptcha();
                captcha.setLen(16);
                captcha.setCharType(CaptchaAbstract.TYPE_NUM_AND_LOWER);
            }
            break;
            default: {
                captcha = new SpecCaptcha(100, 38, 4);
            }
            break;
        }
        saveCaptcha(captcha, username, captchaType);
        return captcha;
    }

    /**
     * 保存验证码到 redis
     *
     * @param captcha     验证码生成器
     * @param username    用户名
     * @param captchaType 验证码类型
     */
    private void saveCaptcha(CaptchaAbstract captcha, String username, CaptchaTypeEnum captchaType) {
        if (username == null || username.trim().isEmpty()) {
            username = "";
        }
        stringRedisTemplate.opsForValue().setIfAbsent(
                String.format("%s%s:%s", REDIS_CAPTCHA_PREFIX, username, captcha.text().toLowerCase()),
                String.valueOf(System.currentTimeMillis()),
                captchaType.getExpire(),
                TimeUnit.MINUTES
        );
    }
}
