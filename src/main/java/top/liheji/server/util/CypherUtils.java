package top.liheji.server.util;

import top.liheji.server.constant.MessageDigestEnum;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * @author : Galaxy
 * @time : 2021/10/29 22:29
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 加密工具类
 */
public class CypherUtils {
    /**
     * 16 进制字符
     */
    public static final String[] BYTE_CHARS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    /**
     * 利用java原生的摘要实现摘要算法
     *
     * @param str 需要加密后报文
     * @return 加密字符串
     */
    public static String encodeToHash(String str, MessageDigestEnum algorithm) {
        return encodeToHash(str.getBytes(StandardCharsets.UTF_8), algorithm);
    }

    /**
     * 实现Base64加密字符串
     *
     * @param str 需要加密的报文
     * @return 加密字符串
     */
    public static String encodeToBase64(String str) {
        return encodeToBase64(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 实现Base64解密字符串
     *
     * @param str 需要加密的报文
     * @return 字节流
     */
    public static byte[] decodeToBytes(String str) {
        return decodeToBytes(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 利用java原生的摘要实现SHA256加密文字流
     *
     * @param bytes 需要加密后报文
     * @return 加密字符串
     */
    public static String encodeToHash(byte[] bytes, MessageDigestEnum algorithm) {
        MessageDigest messageDigest;
        messageDigest = algorithm.messageDigest();
        messageDigest.update(bytes);
        return bytesToString(messageDigest.digest());
    }

    /**
     * 实现Base64加密字符串
     *
     * @param bytes 需要加密的报文文字流
     * @return 加密字符串
     */
    public static String encodeToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 实现Base64解密字符串
     *
     * @param bytes 需要解密的报文文字流
     * @return 字节流
     */
    public static byte[] decodeToBytes(byte[] bytes) {
        return Base64.getDecoder().decode(bytes);
    }

    /**
     * 将byte转为16进制
     *
     * @param bytes 数据
     * @return 转化的字符串
     */
    public static String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(BYTE_CHARS[(b >> 4) & 15]);
            sb.append(BYTE_CHARS[b & 15]);
        }
        return sb.toString();
    }
}
