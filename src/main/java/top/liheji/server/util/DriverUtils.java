package top.liheji.server.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.File;
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

    private WebDriver webDriver;
    private FirefoxOptions firefoxOptions;

    /**
     * WebDriver 私有化构造器，只能创建一个
     */
    private DriverUtils() {
        File file;
        if (SystemUtils.isWindows()) {
            file = FileUtils.resourceFile("drivers", "geckodriver_win.exe");
        } else if (SystemUtils.isLinux()) {
            file = FileUtils.resourceFile("drivers", "geckodriver_linux");
        } else {
            throw new RuntimeException("Platform not supported！");
        }

        ConsoleUtils.authorize(file.getAbsolutePath());

        //设置 geckodriver路径
        System.setProperty("webdriver.gecko.driver", file.getAbsolutePath());
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
