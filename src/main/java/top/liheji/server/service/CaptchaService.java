package top.liheji.server.service;

import top.liheji.server.pojo.Account;

/**
 * @author : Galaxy
 * @time : 2022/1/17 21:28
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 验证码生成服务
 */
public interface CaptchaService {
    
    /**
     * 生成图片验证码
     *
     * @param len    验证码长度
     * @param width  图片宽度
     * @param height 图片高度
     * @return 图片数据
     * @throws Exception 异常
     */
    String genImgCaptcha(int len, int width, int height) throws Exception;

    /**
     * 邮箱发送验证码
     *
     * @param receiver 接收者
     */
    void sendEmailCaptcha(String receiver);

    /**
     * 电话发送验证码
     *
     * @param receiver 接收者
     */
    void sendMobileCaptcha(String receiver);

    /**
     * 返回验证码
     *
     * @param len 验证码长度
     * @return 验证码
     */
    String genCaptcha(int len);

    /**
     * 检查验证码
     *
     * @param code 验证码
     * @return 检验是否通过
     */
    boolean checkCaptcha(String code);

    /**
     * 生成秘钥
     *
     * @param account 用户
     * @param expire  过期时间
     * @return key
     */
    String genSecret(Account account, long expire);

    /**
     * 检查秘钥
     *
     * @param account 用户
     * @param key     秘钥Key
     * @return 检验是否通过
     */
    boolean checkSecret(Account account, String key);
}
