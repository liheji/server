package top.liheji.server.util;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import top.liheji.server.pojo.FileAttr;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Stack;

/**
 * @author : Galaxy
 * @time : 2021/10/29 22:29
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 文件相关工具类
 */
@Slf4j
public class FileUtils {
    /**
     * 批量保存文件
     *
     * @param files 文件
     * @return 文件信息
     * @throws Exception IOException
     */
    public static FileAttr[] uploadFiles(MultipartFile[] files) throws Exception {
        FileAttr[] infos = new FileAttr[files.length];
        for (int i = 0; i < files.length; i++) {
            infos[i] = uploadFile(files[i]);
        }
        return infos;
    }

    /**
     * 保存文件
     *
     * @param file 文件
     * @return 文件信息
     * @throws Exception IOException
     */
    public static FileAttr uploadFile(MultipartFile file) throws Exception {
        File dirFile = resourceFile("files");

        //不存在则创建files文件夹
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        FileAttr info = new FileAttr();
        // 文件存放服务端的位置
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            fileName = "";
        }

        File f = new File(dirFile, fileName);

        //重名
        int i = 1;
        int index = fileName.lastIndexOf(".");
        while (f.exists()) {
            //重新生成目录
            if (index < 0) {
                f = new File(dirFile, String.format("%s(%d", fileName, i));
            } else {
                f = new File(dirFile, String.format("%s(%d)%s", fileName.substring(0, index), i, fileName.substring(index)));
            }
            i++;
        }

        info.setFileName(f.getName());
        info.setFileSize(file.getSize());

        //为读取文件提供流通道
        @Cleanup InputStream in = file.getInputStream();
        @Cleanup OutputStream out = new FileOutputStream(f);
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");

        int num;
        byte[] bytes = new byte[1024];
        while ((num = in.read(bytes)) != -1) {
            out.write(bytes, 0, num);
            messageDigest.update(bytes, 0, num);
        }

        info.setFileHash(CypherUtils.bytesToString(messageDigest.digest()));

        return info;
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

        File writeFile = genNoRepeatFile(".png", "files");

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
        File resFile;
        try {
            resFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX);
        } catch (Exception e) {
            URL url = Thread.currentThread().getContextClassLoader().getResource("");
            if (url != null) {
                resFile = new File(url.getPath());
            } else {
                resFile = null;
            }
        }

        if (args.length > 0) {
            resFile = new File(resFile, String.join(File.separator, args));
        }

        return resFile;
    }

    /**
     * 获取文件MimeType
     *
     * @param file 文件
     * @return 文件的 MimeType
     */
    public static String fileMimeType(File file) {
        String contentType = "";
        Path path = Paths.get(file.getAbsolutePath());
        try {
            contentType = Files.probeContentType(path);
        } catch (IOException e) {
            log.warn(e.toString());
        } finally {
            if ("".equals(contentType)) {
                contentType = fileType(file);
            }
        }

        return contentType;
    }

    /**
     * 获取文件MimeType
     *
     * @param file 文件
     * @return 文件的 MimeType
     */
    private static String fileType(File file) {
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
    private static String[] splitText(String filePath) {
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
     * @param args 路径参数列表
     * @return 格式化后的路径
     */
    public static String join(String... args) {
        String prefix = "";
        if (args.length > 0 && args[0].contains(":")) {
            int i = args[0].indexOf(":");
            prefix = args[0].substring(0, i + 1);
            args[0] = args[0].substring(i + 1);
        }

        Stack<String> stk = new Stack<>();
        for (String arg : args) {
            if (arg.startsWith("/") || arg.startsWith("\\")) {
                stk.clear();
            }

            String[] paths = arg.split("\\s*[/\\\\]+\\s*");
            for (String path : paths) {
                if ("".equals(path) || ".".equals(path)) {
                    continue;
                }

                if ("..".equals(path)) {
                    if (!stk.empty()) {
                        stk.pop();
                    }
                } else {
                    stk.push(path);
                }
            }
        }

        if (stk.empty()) {
            return prefix + "/";
        }

        StringBuilder builder = new StringBuilder();
        while (!stk.empty()) {
            builder.insert(0, stk.pop()).insert(0, "/");
        }

        return prefix + builder;
    }
}
