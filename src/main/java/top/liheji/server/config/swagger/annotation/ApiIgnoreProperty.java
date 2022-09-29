package top.liheji.server.config.swagger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : Galaxy
 * @time : 2022/9/14 18:23
 * @create : IdeaJ
 * @project : server
 * @description :
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiIgnoreProperty {
}
