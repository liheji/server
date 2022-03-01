package top.liheji.server.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Time : 2022/1/22 22:22
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : mybatis-gen
 * @Description : MybatisPlus配置类
 */
@Slf4j
@Configuration
public class MybatisPlusConfig {

    /**
     * 加载分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //添加分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        //添加乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        log.info("MybatisPlus拦截器（分页功能和乐观锁插件）注入成功");

        return interceptor;
    }
}
