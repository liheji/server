package top.liheji.server.util;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Date;

/**
 * @Time : 2022/2/3 15:15
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : serverPlus
 * @Description :
 */
public class EmailUtils {
    public static void sendCaptcha(JavaMailSender sender, String receiver, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("SERVER 邮箱验证");
        message.setFrom("2820054672@qq.com");
        message.setTo(receiver);
        message.setSentDate(new Date());
        message.setText(String.format("你申请的验证码为：%s\n有效期10分钟\n请尽快输入你的验证码\n", code));
        sender.send(message);
    }
}
