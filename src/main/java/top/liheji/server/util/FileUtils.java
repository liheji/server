package top.liheji.server.util;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;
import org.springframework.web.multipart.MultipartFile;
import top.liheji.server.pojo.FileInfo;

import java.io.*;
import java.nio.file.Paths;
import java.security.MessageDigest;

/**
 * @author : Galaxy
 * @time : 2021/10/29 22:29
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 文件相关工具类
 */
@Slf4j
public class FileUtils {
    private static final String RESOURCE_DIR = "/usr/local/tomcat/resources";

    /**
     * 保存文件
     *
     * @param file 文件
     * @return 文件信息
     * @throws Exception IOException
     */
    public static FileInfo uploadFile(MultipartFile file) throws Exception {
        File dirFile = resourceFile("uploads");

        //不存在则创建files文件夹
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        // 文件存放服务端的位置
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            fileName = "";
        }

        File f = genNoRepeatFile(splitText(fileName)[1], "uploads");

        //为读取文件提供流通道
        @Cleanup InputStream in = file.getInputStream();
        @Cleanup OutputStream out = new FileOutputStream(f);
        MessageDigest digestMd5 = MessageDigest.getInstance("MD5");
        MessageDigest digestSSha256 = MessageDigest.getInstance("SHA-256");

        int num;
        byte[] bytes = new byte[1024];
        while ((num = in.read(bytes)) != -1) {
            out.write(bytes, 0, num);
            digestMd5.update(bytes, 0, num);
            digestSSha256.update(bytes, 0, num);
        }

        return new FileInfo(
                f.getName(),
                file.getSize(),
                CypherUtils.bytesToString(digestMd5.digest()) + CypherUtils.bytesToString(digestSSha256.digest())
        );
    }

    /**
     * 文件写入
     *
     * @param out 输出流
     * @param raf 输出流
     * @param pos 偏移
     * @param len 长度
     * @throws IOException IOException
     */
    public static void writePos(OutputStream out, RandomAccessFile raf, long pos, long len) throws IOException {
        raf.seek(pos);

        long total = 0L;
        byte[] buffer = new byte[10240];
        while (true) {
            int res = raf.read(buffer);
            if (res <= 0) {
                break;
            }

            total += res;
            if (out != null) {
                if (total < len) {
                    out.write(buffer, 0, res);
                } else {
                    out.write(buffer, 0, (int) (len - total + res));
                }
            }
        }
    }

    /**
     * 文件写入
     *
     * @param bs64 base64编码文件
     * @throws IOException IOException
     */
    public static File base64SaveToFile(String bs64) throws IOException {
        if (bs64.startsWith("data:")) {
            bs64 = bs64.replaceFirst("^data:.+?;base64,", "");
        }

        File writeFile = genNoRepeatFile(".png", "uploads");

        @Cleanup InputStream in = new ByteArrayInputStream(CypherUtils.decodeToBytes(bs64));
        @Cleanup OutputStream out = new FileOutputStream(writeFile);

        int len = 0;
        byte[] buffer = new byte[1024];
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }

        return writeFile;
    }

    /**
     * 生成一个在文件夹中不存在的文件名并返回文件类
     *
     * @param suffix 文件后缀名
     * @param args   文件夹
     * @return 文件
     */
    public static File genNoRepeatFile(String suffix, String... args) {
        File baseDir = resourceFile(args);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        File file = new File(baseDir, StringUtils.genUuidWithoutLine() + suffix);
        while (file.exists()) {
            file = new File(baseDir, StringUtils.genUuidWithoutLine() + suffix);
        }

        return file;
    }

    /**
     * 获取Resource目录下的文件或文件夹
     * 不创建不存在的文件或文件夹
     *
     * @param args 文件子路径
     * @return 文件
     */
    public static File resourceFile(String... args) {
        return Paths.get(RESOURCE_DIR, args).toFile();
    }

    /**
     * 获取文件MimeType
     *
     * @param file 文件
     * @return 文件的 MimeType
     */
    public static String guessMediaType(File file) {
        String contentType = MediaType.guessMediaType(file.getName());
        if ("application/octet-stream".equals(contentType)) {
            contentType = magicGuessFileMediaType(file);
        }
        return contentType;
    }

    /**
     * 获取文件MimeType
     *
     * @param file 文件
     * @return 文件的 MimeType
     */
    private static String magicGuessFileMediaType(File file) {
        String contentType = "";
        try {
            MagicMatch match = Magic.getMagicMatch(file, false);
            contentType = match.getMimeType();
        } catch (Exception e) {
            log.warn(e.toString());
        } finally {
            if ("".equals(contentType)) {
                contentType = "application/octet-stream";
            }
        }
        return contentType;
    }

    /**
     * 将字符串切割为文件名和后缀
     *
     * @param filePath 文件路径
     * @return 文件文件名和后缀
     */
    public static String[] splitText(String filePath) {
        return splitText(new File(filePath));
    }

    /**
     * 将字符串切割为文件名和后缀
     *
     * @param file 文件
     * @return 文件文件名和后缀
     */
    private static String[] splitText(File file) {
        String fileName = file.getName();
        int index = fileName.lastIndexOf(".");
        if (index < 0) {
            index = fileName.length();
        }

        return new String[]{
                new File(file.getParentFile(), fileName.substring(0, index)).getAbsolutePath(),
                fileName.substring(index)
        };
    }

    /**
     * 路径格式化
     *
     * @param first 父路径
     * @param more  子路径
     * @return 格式化后的路径
     */
    public static String join(String first, String... more) {
        return Paths.get(first, more).toFile().getAbsolutePath();
    }
}
