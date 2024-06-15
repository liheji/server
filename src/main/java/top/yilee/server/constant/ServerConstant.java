
package top.yilee.server.constant;

import top.yilee.server.pojo.Account;
import top.yilee.server.pojo.AuthDevices;


/**
 * @author Galaxy
 */
public class ServerConstant {
    // FileUtils静态变量
    /**
     * web 静态资源路径
     */
    public static final String STATIC_DIR = "/data/resources";

    /**
     * resources 资源路径
     */
    public static final String RESOURCE_DIR = "/application/BOOT-INF/classes";

    /**
     * 线程共享信息（同一个线程）
     */
    public static final ThreadLocal<String> LOCAL_SERIES = new ThreadLocal<>();
    public static final ThreadLocal<Account> LOCAL_ACCOUNT = new ThreadLocal<>();
    public static final ThreadLocal<AuthDevices> LOCAL_DEVICE = new ThreadLocal<>();
}
