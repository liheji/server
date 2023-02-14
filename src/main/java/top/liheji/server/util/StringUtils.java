package top.liheji.server.util;

import org.springframework.util.ObjectUtils;

import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : Galaxy
 * @time : 2022/1/17 14:00
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 字符串工具类
 */
public class StringUtils {
    public static final Pattern HTTP_RANGE_HEADER = Pattern.compile("bytes=(\\d*)-(\\d*)");

    public static final char[] UUID_CHARS = {'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
            't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
            'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
            'W', 'X', 'Y', 'Z'};

    /**
     * 获取一个去掉横线的UUID字符串
     *
     * @return UUID字符串
     */
    public static String getUuidNoLine() {
        return getUuid().replaceAll("-", "");
    }

    /**
     * 获取一个原始UUID字符串
     *
     * @return UUID字符串
     */
    public static String getUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成一个随机字符串（字母或下划线开头）
     *
     * @param len 字符串长度
     * @return 生成的字符串
     */
    public static String getRandString(int len) {
        Random random = new Random();

        StringBuilder result = new StringBuilder();
        // 生成第一个字符
        while (true) {
            char chr = UUID_CHARS[random.nextInt(UUID_CHARS.length)];
            if (Character.isLetter(chr)) {
                result.append(chr);
                break;
            }
        }
        // 继续放入剩下的字符
        for (int i = 1; i < len; i++) {
            result.append(UUID_CHARS[random.nextInt(UUID_CHARS.length)]);
        }

        return result.toString();
    }

    /**
     * 解析HTTP Range头
     *
     * @param range range头
     * @param total 总长度
     * @return 返回解析结果
     */
    public static long[] parseRange(String range, long total) {
        long[] pos = new long[]{0, total - 1};

        // 有range参数
        if(ObjectUtils.isEmpty(range)) {
            return pos;
        }
        Matcher m = HTTP_RANGE_HEADER.matcher(range);
        if (m.find()) {
            String start = m.group(1).trim();
            String end = m.group(2).trim();
            if (start.isEmpty() && !end.isEmpty()) {
                pos[0] = pos[1] - Long.parseLong(end) + 1;
            } else {
                if (!start.isEmpty()) {
                    pos[0] = Long.parseLong(start);
                }
                if (!end.isEmpty()) {
                    pos[1] = Long.parseLong(end);
                }
                if (pos[0] > pos[1]) {
                    pos[0] = 0L;
                    pos[1] = total - 1;
                }
            }
        }

        return pos;
    }
}
