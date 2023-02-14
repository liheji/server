package top.liheji.server.service;

import top.liheji.server.constant.CaptchaTypeEnum;

/**
 * @author : Galaxy
 * @time : 2022/1/17 21:28
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 验证码生成服务
 */
public interface CaptchaService {
    /**
     * 生成文字验证码
     *
     * @return 文字验证码
     */
    String genCaptcha(String username, CaptchaTypeEnum captchaType);

    /**
     * 生成图片验证码
     *
     * @return 图片验证码
     */
    String genImgCaptcha(String username, CaptchaTypeEnum captchaType);

    /**
     * 生发送验证码(长度4，存活时间 10 min）
     *
     * @param username    接收者
     * @param captchaType 发送类型
     */
    void sendCaptcha(String username, String receiver, CaptchaTypeEnum captchaType);

    /**
     * 检查验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @return 检验是否通过
     */
    boolean checkCaptcha(String username, String code);
}
