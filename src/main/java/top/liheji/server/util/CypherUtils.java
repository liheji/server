package top.liheji.server.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * @Time : 2021/10/29 22:29
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : server
 * @Description : 加密工具
 */
public class CypherUtils {

    /**
     * 利用java原生的摘要实现SHA256加密
     *
     * @param str 需要加密后报文
     * @return 加密字符串
     */
    public static String getSha256Str(String str) {
        return getSha256Str(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 利用java原生的摘要实现MD5加密
     *
     * @param str 需要加密后的报文
     * @return 加密字符串
     */
    public static String getMd5Str(String str) {
        return getMd5Str(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 实现Base64加密字符串
     *
     * @param str 需要加密的报文
     * @return 加密字符串
     */
    public static String getBase64Str(String str) {
        return getBase64Str(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 利用java原生的摘要实现SHA256加密文字流
     *
     * @param bytes 需要加密后报文
     * @return 加密字符串
     */
    public static String getSha256Str(byte[] bytes) {
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(bytes);
            encodeStr = bytesToString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    /**
     * 利用java原生的摘要实现MD5加密
     *
     * @param bytes 需要加密后的报文字流
     * @return 加密字符串
     */
    public static String getMd5Str(byte[] bytes) {
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(bytes);
            encodeStr = bytesToString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    /**
     * 实现Base64加密字符串
     *
     * @param bytes 需要加密的报文文字流
     * @return 加密字符串
     */
    public static String getBase64Str(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 将byte转为16进制
     *
     * @param bytes 数据
     * @return 转化的字符串
     */
    public static String bytesToString(byte[] bytes) {
        char[] chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            sb.append(chars[(b >> 4) & 15]);
            sb.append(chars[b & 15]);
        }
        return sb.toString();
    }

    /**
     * 获取一个去掉横线的UUID字符串
     *
     * @return UUID字符串
     */
    public static String genUuid() {
        return genUuidWithLine().replaceAll("-", "");
    }

    /**
     * 获取一个原始UUID字符串
     *
     * @return UUID字符串
     */
    public static String genUuidWithLine() {
        return UUID.randomUUID().toString();
    }
}
