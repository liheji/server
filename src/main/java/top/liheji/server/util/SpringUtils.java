package top.liheji.server.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @Time : 2022/3/2 11:04
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : serverPlus
 * @Description :
 */
@Component
public class SpringUtils implements ApplicationContextAware {
    private static ApplicationContext applicationContext = null;

    public SpringUtils() {
    }

    @Override
    public void setApplicationContext(ApplicationContext arg0) throws BeansException {
        if (applicationContext == null) {
            applicationContext = arg0;
        }
    }

    /**
     * 拿到ApplicationContext对象实例后就可以手动获取Bean的注入实例对象
     */
    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static Object getBean(String beanName, String className) throws ClassNotFoundException {
        Class<?> clz = Class.forName(className);
        return applicationContext.getBean(beanName, clz);
    }

    public static <T> T getBean(String name, Class<T> clazz) throws ClassNotFoundException {
        return applicationContext.getBean(name, clazz);
    }
}