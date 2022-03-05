package top.liheji.server.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import top.liheji.server.pojo.Account;
import top.liheji.server.service.CaptchaService;
import top.liheji.server.util.CypherUtils;
import top.liheji.server.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
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
    private RedisTemplate<String, Object> redisTemplate;

    private static final Random RANDOM = new Random();
    private static final String CAPTCHA_CHARS = "0123456798qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";

    @Override
    public String genCaptcha(int len) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < len; ++j) {
            builder.append(CAPTCHA_CHARS.charAt(RANDOM.nextInt(CAPTCHA_CHARS.length())));
        }

        //存储到 redis(大小写不敏感)
        String code = builder.toString().toLowerCase();
        redisTemplate.opsForValue().setIfAbsent("captcha:" + code, "", 10 * 60L, TimeUnit.SECONDS);

        return code;
    }

    @Override
    public String genImgCaptcha(int len, int width, int height) throws Exception {
        //生成验证码并存储到 redis
        String code = genCaptcha(len);
        //生成验证码图片并返回
        return genCaptchaImage(code, width, height);
    }

    @Override
    public boolean checkCaptcha(String code) {
        if (code == null) {
            return false;
        }

        String key = "captcha:" + code.toLowerCase();

        Object obj = redisTemplate.opsForValue().get(key);
        if (obj != null) {
            redisTemplate.delete(key);
        }

        return obj != null;
    }

    @Override
    public String genSecret(Account account, long expire) {
        //生成秘钥
        String key = StringUtils.genUuidWithoutLine().toLowerCase();

        //存储到 redis
        redisTemplate.opsForValue().setIfAbsent("secret:" + key, account.getUsername(), expire, TimeUnit.SECONDS);

        return key;
    }

    @Override
    public boolean checkSecret(Account account, String code) {
        if (code == null) {
            return false;
        }

        String key = "secret:" + code.toLowerCase();
        Object obj = redisTemplate.opsForValue().get(key);
        if (obj != null) {
            String uname = (String) obj;
            redisTemplate.delete(key);
            if (account != null) {
                return account.getUsername().equals(uname);
            }

            return true;
        }

        return false;
    }

    @Override
    public void sendEmailCaptcha(String receiver) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("SERVER 邮箱验证");
        message.setFrom("2820054672@qq.com");
        message.setTo(receiver);
        message.setSentDate(new Date());
        message.setText(String.format("你申请的验证码为：%s\n10分钟内有效\n请尽快输入你的验证码\n", genCaptcha(6).toUpperCase()));
        javaMailSender.send(message);
    }

    /**
     * 绘制验证码图片
     *
     * @param code   验证码字符串
     * @param width  验证码图片宽度
     * @param height 验证码图片高度
     * @return 验证码 ID
     * @throws Exception 异常
     */
    private String genCaptchaImage(String code, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();


        //设置验证码图像背景
        graphics.setColor(randomColor(180, 240));
        graphics.fillRect(0, 0, width, height);

        //绘制验证码
        for (int i = 0; i < code.length(); i++) {
            char chr = code.charAt(i);
            //设置字体
            graphics.setColor(randomColor(50, 160));

            int fontsize = height;
            if (chr < 'a' || chr > 'z') {
                fontsize = (int) Math.round(fontsize / 4.0 * 3.0);
            }
            Font newFont = new Font("SimHei", Font.PLAIN, fontsize);
            graphics.setFont(newFont);

            //绘制单个验证码
            int x = (int) Math.round(width / 20.0 + width * 0.23 * i);
            int y = (int) Math.round(height / 5.0 * 4.0);

            int deg = random(-20, 20);
            graphics.translate(x, y);
            graphics.rotate(deg * Math.PI / 180);
            graphics.drawString(chr + "", 0, 0);
            graphics.rotate(-deg * Math.PI / 180);
            graphics.translate(-x, -y);
        }

        //绘制干扰直线
        for (int i = 0; i < 4; i++) {
            graphics.setColor(randomColor(40, 180));
            graphics.drawLine(RANDOM.nextInt(width), RANDOM.nextInt(height), RANDOM.nextInt(width), RANDOM.nextInt(height));
        }

        //绘制噪点
        for (int i = 0; i < width / 4; i++) {
            graphics.setColor(randomColor(0, 255));
            graphics.fillRect(RANDOM.nextInt(width), RANDOM.nextInt(height), 2, 2);
        }

        return encodeImageToBase64(image);
    }

    /**
     * BufferedImage 转化为URL类型的BASE64编码
     *
     * @param image 图片
     * @return BASE64编码数据
     * @throws IOException 异常
     */
    private static String encodeImageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", stream);
        return String.format("data:image/%s;base64,%s", "jpg", CypherUtils.encodeToBase64(stream.toByteArray()));
    }

    /**
     * 随机获取颜色
     *
     * @param min RGB最小值
     * @param max RGB最大值
     * @return 生成的颜色
     */
    private static Color randomColor(int min, int max) {
        int r = random(min, max);
        int g = random(min, max);
        int b = random(min, max);
        return new Color(r, g, b);
    }

    /**
     * 生成min到max之间的随机数
     *
     * @param min 最小值
     * @param max 最大值
     * @return 随机数
     */
    private static int random(int min, int max) {
        return RANDOM.nextInt(max - min) + min;
    }
}
