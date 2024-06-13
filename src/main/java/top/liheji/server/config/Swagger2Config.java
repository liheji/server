package top.liheji.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author : Galaxy
 * @time : 2022/9/14 9:45
 * @create : IdeaJ
 * @project : server
 * @description :
 */
@Configuration
@EnableSwagger2
public class Swagger2Config implements WebMvcConfigurer {
    @Value("${debug: false}")
    private Boolean debug;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .enable(debug)
                .groupName("server")
                .select()
                .apis(RequestHandlerSelectors.basePackage("top.liheji.server.controller"))
                .paths(PathSelectors.any())
                .build()
                .ignoredParameterTypes(HttpServletRequest.class, HttpServletResponse.class);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("憶夣的API")
                .description(
                        "本项目项目使用springboot + MyBatis-Plus实现，项目目的主要作为练手<br>" +
                                "前端使用vue2 + element-ui实现（前后端完全分离）在另一个项目<br>" +
                                "访问地址： http://[IP]:[PORT]/doc.html"
                )
                .contact(new Contact("憶夣", "https://blog.yilee.top", "930617673@qq.com"))
                .build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations(
                "classpath:/static/");
        registry.addResourceHandler("doc.html").addResourceLocations(
                "classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations(
                "classpath:/META-INF/resources/webjars/");
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }
}
