package top.liheji.server.util;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @Time : 2021/12/31 16:09
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : serverVue
 * @Description : 验证码生成工具
 */
@Slf4j
public class CaptchaUtils {
    private static final Pattern MATCHER = Pattern.compile("\\d+");
    private static final String[] CHARS = "0,1,2,3,4,5,6,7,9,8,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z".split(",");

    private static final Map<String, Object[]> CAPTCHA_CACHE = new ConcurrentHashMap<>();

    private final Random random = new Random();
    private final List<String> code = new ArrayList<>(4);

    /**
     * 验证码验证
     *
     * @param cid  验证码ID
     * @param code 验证码
     * @return 是否成功
     */
    public static boolean check(String cid, String code) {
        if (CAPTCHA_CACHE.containsKey(cid)) {
            Object[] captcha = CAPTCHA_CACHE.get(cid);
            boolean tmp = ((Long) captcha[2]) > System.currentTimeMillis()
                    && code != null
                    && captcha[0].equals(code.trim().toLowerCase());
            CAPTCHA_CACHE.remove(cid);
            return tmp;
        }
        return false;
    }

    /**
     * 验证码图片获取
     *
     * @param cid 验证码ID
     * @return 图片
     */
    public static String getImageBase64(String cid) {
        if (CAPTCHA_CACHE.containsKey(cid)) {
            return ((String) CAPTCHA_CACHE.get(cid)[1]);
        }
        return null;
    }

    /**
     * 绘制验证码图片
     *
     * @return 验证码ID
     */
    public String genImage() throws Exception {
        return genImage(100, 38);
    }

    /**
     * 绘制验证码图片
     *
     * @param width  验证码图片宽度
     * @param height 验证码图片高度
     * @return 验证码ID
     */
    public String genImage(int width, int height) throws Exception {
        for (int i = 0; i < 3 && !MATCHER.matcher(code.toString()).find(); i++) {
            code.clear();
            for (int j = 0; j < 4; ++j) {
                code.add(CHARS[random.nextInt(CHARS.length)]);
            }
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        //设置验证码图像背景
        graphics.setColor(randomColor(180, 240));
        graphics.fillRect(0, 0, width, height);

        //绘制验证码
        for (int i = 0; i < code.size(); i++) {
            String chr = code.get(i);
            //设置字体
            graphics.setColor(randomColor(50, 160));

            int fontsize = height;
            if (chr.charAt(0) < 'a' || chr.charAt(0) > 'z') {
                fontsize = (int) Math.round(fontsize / 4.0 * 3.0);
            }
            Font newFont = new Font("SimHei", Font.PLAIN, fontsize);
            graphics.setFont(newFont);

            //绘制单个验证码
            int x = (int) Math.round(width / 20.0 + width * 0.23 * i);
            int y = (int) Math.round(height / 5.0 * 4.0);

            int deg = randomNum(-20, 20);
            graphics.translate(x, y);
            graphics.rotate(deg * Math.PI / 180);
            graphics.drawString(chr, 0, 0);
            graphics.rotate(-deg * Math.PI / 180);
            graphics.translate(-x, -y);
        }

        //绘制干扰直线
        for (int i = 0; i < 4; i++) {
            graphics.setColor(randomColor(40, 180));
            graphics.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
        }

        //绘制噪点
        for (int i = 0; i < width / 4; i++) {
            graphics.setColor(randomColor(0, 255));
            graphics.fillRect(random.nextInt(width), random.nextInt(height), 2, 2);
        }

        //删除冗余的验证码
        for (Map.Entry<String, Object[]> it : CAPTCHA_CACHE.entrySet()) {
            if (((Long) it.getValue()[2]) <= System.currentTimeMillis()) {
                CAPTCHA_CACHE.remove(it.getKey());
            }
        }

        //设置验证码图片
        String cid = CypherUtils.genUuid();
        CAPTCHA_CACHE.put(cid,
                new Object[]{
                        String.join("", code).toLowerCase(),
                        imageToBase64(image),
                        System.currentTimeMillis() + 10 * 60 * 1000
                });

        return cid;
    }

    /**
     * BufferedImage 转化为URL类型的BASE64编码
     *
     * @param image 图片
     * @return BASE64编码数据
     * @throws IOException 异常
     */
    private static String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", stream);
        return String.format("data:image/%s;base64,%s", "jpg", CypherUtils.getBase64Str(stream.toByteArray()));
    }

    /**
     * 随机获取颜色
     *
     * @param min RGB最小值
     * @param max RGB最大值
     * @return 生成的颜色
     */
    private Color randomColor(int min, int max) {
        int r = randomNum(min, max);
        int g = randomNum(min, max);
        int b = randomNum(min, max);
        return new Color(r, g, b);
    }

    /**
     * 生成min到max之间的随机数
     *
     * @param min 最小值
     * @param max 最大值
     * @return 随机数
     */
    private int randomNum(int min, int max) {
        return random.nextInt(max - min) + min;
    }
}
