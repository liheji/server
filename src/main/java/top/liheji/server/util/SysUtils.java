package top.liheji.server.util;

/**
 * @author : Galaxy
 * @time : 2022/8/17 14:28
 * @create : IdeaJ
 * @project : serverPlus
 * @description :
 */
public class SysUtils {
    private static final String OS = System.getProperty("os.name").toLowerCase();


    public static boolean isLinux() {
        return OS.contains("linux");
    }

    public static boolean isMacOS() {
        return OS.contains("mac") && OS.indexOf("os") > 0 && !OS.contains("x");
    }

    public static boolean isMacOSX() {
        return OS.contains("mac") && OS.indexOf("os") > 0 && OS.indexOf("x") > 0;
    }

    public static boolean isWindows() {
        return OS.contains("windows");
    }

    public static boolean isSolaris() {
        return OS.contains("solaris");
    }
}
