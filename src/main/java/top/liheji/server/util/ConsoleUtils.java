package top.liheji.server.util;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author : Galaxy
 * @time : 2022/3/10 10:03
 * @create : IdeaJ
 * @project : serverPlus
 * @description :
 */
@Slf4j
public class ConsoleUtils {
    public static final String SERVER_STATIC_PATH = "/usr/local/tomcat/webapps/static";

    /**
     * 执行命令
     *
     * @param cmd 命令
     * @return 返回执行结果
     */
    public static String execute(String cmd) {
        String[] cmdArr = {"/bin/sh", "-c", cmd};
        Runtime run = Runtime.getRuntime();
        try {
            @Cleanup("destroy") Process process = (cmd != null && cmd.contains("|")) ? run.exec(cmdArr) : run.exec(cmd);
            @Cleanup InputStream in = process.getInputStream();
            StringBuilder builder = new StringBuilder();

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = in.read(bytes)) != -1) {
                builder.append(new String(bytes, 0, len));
            }

            return builder.toString();
        } catch (IOException e) {
            log.error("远程代码执行错误：" + e);
        }
        return null;
    }

    /**
     * 文件授权
     *
     * @param filePath 授权文件路径
     */
    public static void authorize(String filePath) {
        String res = execute(String.format("ls -l %s|awk '{print $1}'", filePath));
        if (res == null || !res.contains("x")) {
            execute(String.format("chmod +x %s", filePath));
        }
    }
}
