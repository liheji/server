package top.liheji.server.util;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @author : Galaxy
 * @time : 2021/10/29 22:29
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 浏览器驱动工具类
 */
@Slf4j
public class DriverUtils {
    private static DriverUtils driver;
    private static boolean isPermit = false;
    private static final String OS = System.getProperty("os.name").toLowerCase();

    private WebDriver webDriver;
    private FirefoxOptions firefoxOptions;

    /**
     * WebDriver 私有化构造器，只能创建一个
     */
    private DriverUtils() {
        String fileName;
        if (OS.contains("windows")) {
            fileName = "geckodriver_win.exe";
        } else if (OS.contains("linux")) {
            fileName = "geckodriver_linux";
        } else {
            throw new RuntimeException("Platform not supported！");
        }

        File driverFile = FileUtils.resourceFile("data", fileName);

        //对 geckodriver_linux执行授权
        if (OS.contains("linux") && !isPermit) {
            String res = executeCmd(String.format("ls -l %s|awk '{print $1}'", driverFile.getAbsolutePath()));

            if (res == null || !res.contains("x")) {
                executeCmd(String.format("chmod +x %s", driverFile.getAbsolutePath()));
            }
            isPermit = true;
        }

        //设置 geckodriver路径
        System.setProperty("webdriver.gecko.driver", driverFile.getAbsolutePath());
    }


    /**
     * 获取当前实例
     *
     * @return 单一实例
     */
    public static DriverUtils getInstance() {
        if (driver == null) {
            synchronized (DriverUtils.class) {
                if (driver == null) {
                    driver = new DriverUtils();
                }
            }
        }
        return driver;
    }

    /**
     * 获取一个WebDriver
     *
     * @return WebDriver
     */
    public WebDriver getWebDriver() {
        if (webDriver == null) {
            webDriver = new FirefoxDriver(getFirefoxOptions());
            //设置隐式等待
            webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        }
        return webDriver;
    }

    /**
     * 获取Driver设置选项
     *
     * @return ChromeOptions;
     */
    public FirefoxOptions getFirefoxOptions() {
        if (firefoxOptions == null) {
            //设置option
            firefoxOptions = new FirefoxOptions();
            //无界面模式
            firefoxOptions.setHeadless(true);
            //隐身模式（无痕模式）
            firefoxOptions.addArguments("--incognito");
            //解决DevToolsActivePort文件不存在的报错
            firefoxOptions.addArguments("--no-sandbox");
            //禁用插件
            firefoxOptions.addArguments("disable-plugins");
            //禁止策略化(禁用浏览器正在被自动化程序控制的提示)
            firefoxOptions.addArguments("disable-infobars");
            //不加载图片, 提升速度
            firefoxOptions.addArguments("blink-settings=imagesEnabled=false");
        }

        return firefoxOptions;
    }


    /**
     * 为当前WebDriver 添加属性
     *
     * @param args 参数
     */
    public void addArguments(String... args) {
        if (firefoxOptions == null) {
            getFirefoxOptions();
        }
        firefoxOptions.addArguments(args);
    }

    /**
     * 关闭浏览器连接
     */
    public void close() {
        if (webDriver != null) {
            webDriver.close();
            webDriver.quit();
            webDriver = null;
        }
    }

    /**
     * 关闭浏览器连接
     */
    public void timeClose() {
        new Timer().schedule(new CloseTask(), 20 * 60 * 1000);
    }

    /**
     * 执行命令
     *
     * @param cmd 命令
     * @return 返回执行结果
     */
    private static String executeCmd(String cmd) {
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


    private static class CloseTask extends TimerTask {
        private static long CLOSE_TIME = 0;

        public CloseTask() {
            CLOSE_TIME = System.currentTimeMillis() + 20 * 60 * 1000;
        }

        @SneakyThrows
        @Override
        public void run() {
            if (CLOSE_TIME > 0 && CLOSE_TIME <= System.currentTimeMillis()) {
                driver.close();
                CLOSE_TIME = 0;
            }
        }
    }
}
