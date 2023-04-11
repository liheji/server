package top.liheji.server.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.IdUtil;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import top.liheji.server.constant.ServerConstant;
import top.liheji.server.pojo.FileInfo;

import java.io.*;
import java.net.URL;
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
    /**
     * 保存文件
     *
     * @param file 文件
     * @return 文件信息
     * @throws Exception IOException
     */
    public static FileInfo uploadFile(MultipartFile file) throws Exception {
        File dirFile = staticFile("uploads");

        //不存在则创建files文件夹
        FileUtil.mkdir(dirFile);

        // 文件存放服务端的位置
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            fileName = "";
        }

        File f = getUniqueFile("." + FileNameUtil.extName(fileName), "uploads");

        //为读取文件提供流通道
        @Cleanup InputStream in = file.getInputStream();
        @Cleanup OutputStream out = new FileOutputStream(f);

        MessageDigest digestMd5 = MessageDigest.getInstance("MD5");
        MessageDigest digestSha256 = MessageDigest.getInstance("SHA-256");

        int num;
        byte[] bytes = new byte[1024];
        while ((num = in.read(bytes)) != -1) {
            out.write(bytes, 0, num);
            digestMd5.update(bytes, 0, num);
            digestSha256.update(bytes, 0, num);
        }

        return new FileInfo(
                f.getName(),
                file.getSize(),
                HexUtil.encodeHexStr(digestMd5.digest()) + HexUtil.encodeHexStr(digestSha256.digest())
        );
    }

    /**
     * 文件写入
     *
     * @param file 文件
     * @param out  输出流
     * @param pos  偏移
     * @param len  长度
     * @throws IOException IOException
     */
    public static void writePos(File file, OutputStream out, long pos, long len) throws IOException {
        if (out == null) {
            return;
        }
        //构建任意读取输入流
        @Cleanup RandomAccessFile raf = new RandomAccessFile(file, "r");
        raf.seek(pos);

        long total = 0L;
        byte[] buffer = new byte[1024];
        while (true) {
            int res = raf.read(buffer);
            if (res <= 0 || total >= len) {
                break;
            }

            total += res;
            if (total <= len) {
                out.write(buffer, 0, res);
            } else {
                out.write(buffer, 0, (int) (len - total + res));
            }
        }
    }

    /**
     * 生成一个在文件夹中不存在的文件名并返回文件类
     *
     * @param suffix 文件后缀名
     * @param args   文件夹
     * @return 文件
     */
    public static File getUniqueFile(String suffix, String... args) {
        File baseDir = staticFile(args);
        FileUtil.mkdir(baseDir);

        File file = new File(baseDir, IdUtil.simpleUUID() + suffix);
        while (file.exists()) {
            file = new File(baseDir, IdUtil.simpleUUID() + suffix);
        }

        return file;
    }

    /**
     * 获取服务器指定路径下的资源
     * 不创建不存在的文件或文件夹
     *
     * @param args 文件子路径
     * @return 文件
     */
    public static File staticFile(String... args) {
        return Paths.get(ServerConstant.STATIC_DIR, args).toFile();
    }

    /**
     * 获取项目 resources目录下的文件或文件夹
     * 不创建不存在的文件或文件夹
     *
     * @param args 文件子路径
     * @return 文件
     */
    public static File resourceFile(String... args) {
        File resFile = null;
        try {
            resFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX);
        } catch (Exception e) {
            URL url = Thread.currentThread().getContextClassLoader().getResource("");
            if (url != null) {
                resFile = new File(url.getPath());
            }
        } finally {
            if (resFile == null || resFile.exists()) {
                resFile = new File(ServerConstant.RESOURCE_DIR);
            }
        }

        if (args.length > 0) {
            resFile = new File(resFile, String.join(File.separator, args));
        }

        return resFile;
    }
}
