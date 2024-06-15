package top.yilee.server.util;

import lombok.NoArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : Galaxy
 * @time : 2022/3/2 11:04
 * @create : IdeaJ
 * @project : serverPlus
 * @description : Spring运行时获取Bean工具类
 */
@Component
@NoArgsConstructor
public class BeanUtils implements ApplicationContextAware {
    private static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext arg) throws BeansException {
        if (applicationContext == null) {
            applicationContext = arg;
        }
    }

    /**
     * 拿到ApplicationContext对象实例后就可以手动获取Bean的注入实例对象
     */
    public static Object getBean(String var1) throws BeansException {
        return applicationContext.getBean(var1);
    }

    public static <T> T getBean(String var1, Class<T> var2) throws BeansException {
        return applicationContext.getBean(var1, var2);
    }

    public static Object getBean(String var1, Object... var2) throws BeansException {
        return applicationContext.getBean(var1, var2);
    }

    public static <T> T getBean(Class<T> var1) throws BeansException {
        return applicationContext.getBean(var1);
    }

    public static <T> T getBean(Class<T> var1, Object... var2) throws BeansException {
        return applicationContext.getBean(var1, var2);
    }

    public static <T> ObjectProvider<T> getBeanProvider(Class<T> var1) {
        return applicationContext.getBeanProvider(var1);
    }

    public static <T> ObjectProvider<T> getBeanProvider(ResolvableType var1) {
        return applicationContext.getBeanProvider(var1);
    }

    public static boolean containsBean(String var1) {
        return applicationContext.containsBean(var1);
    }

    public static boolean isSingleton(String var1) throws NoSuchBeanDefinitionException {
        return applicationContext.isSingleton(var1);
    }

    public static boolean isPrototype(String var1) throws NoSuchBeanDefinitionException {
        return applicationContext.isPrototype(var1);
    }

    public static boolean isTypeMatch(String var1, ResolvableType var2) throws NoSuchBeanDefinitionException {
        return applicationContext.isTypeMatch(var1, var2);
    }

    public static boolean isTypeMatch(String var1, Class<?> var2) throws NoSuchBeanDefinitionException {
        return applicationContext.isTypeMatch(var1, var2);
    }

    public static Class<?> getType(String var1) throws NoSuchBeanDefinitionException {
        return applicationContext.getType(var1);
    }

    public static Class<?> getType(String var1, boolean var2) throws NoSuchBeanDefinitionException {
        return applicationContext.getType(var1, var2);
    }

    public static String[] getAliases(String var1) {
        return applicationContext.getAliases(var1);
    }


    public static Map<String, Object> toMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                Object value = field.get(obj);
                if (value != null) {
                    map.put(name, value);
                }
            }
        } catch (Exception ignored) {
        }
        return map;
    }
}