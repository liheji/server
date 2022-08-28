package top.liheji.server.service;

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
     * @param receiver 接收者邮箱
     */
    void sendEmailCaptcha(String receiver);

    /**
     * 电话发送验证码
     *
     * @param receiver 接收者手机号
     */
    void sendMobileCaptcha(String receiver);

    /**
     * 检查验证码
     *
     * @param code   验证码
     * @param prefix 前缀
     * @return 检验是否通过
     */
    boolean checkCaptcha(String code, String... prefix);

    /**
     * 返回验证码
     *
     * @param len    验证码长度
     * @param prefix 前缀
     * @return 验证码
     */
    String genCaptcha(int len, String... prefix);

    /**
     * 生成秘钥
     *
     * @param username 用户名
     * @param expire   过期时间
     * @return key
     */
    String genSecret(String username, long expire);

    /**
     * 检查秘钥
     *
     * @param username 用户名
     * @param key      秘钥Key
     * @return 检验是否通过
     */
    boolean checkSecret(String username, String key);
}
