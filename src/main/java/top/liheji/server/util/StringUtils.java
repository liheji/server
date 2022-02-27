package top.liheji.server.util;

import java.util.UUID;

/**
 * @Time : 2022/2/27 16:41
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : serverPlus
 * @Description :
 */
public class StringUtils {
    private static final String[] UUID_CHARS = {"a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z"};

    /**
     * 获取一个去掉横线的UUID字符串
     *
     * @return UUID字符串
     */
    public static String genUuidWithoutLine() {
        return genUuid().replaceAll("-", "");
    }

    /**
     * 获取一个原始UUID字符串
     *
     * @return UUID字符串
     */
    public static String genUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获取一个8位UUID字符串
     *
     * @return UUID字符串
     */
    public static String genShortUuid() {
        String uid = genUuidWithoutLine();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            String sub = uid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(sub, 16);
            result.append(UUID_CHARS[x % 0x3E]);
        }

        return result.toString();
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }


    /**
     * 将英文转化为单词样式（eg: good => Good）
     *
     * @param str 转换前的字符
     * @return 转化完成的字符
     */
    public static String toWord(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
