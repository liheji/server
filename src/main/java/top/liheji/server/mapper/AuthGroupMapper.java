package top.liheji.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import top.liheji.server.config.mybatis.cache.MybatisPlusRedisCache;
import top.liheji.server.pojo.AuthGroup;

/**
 * @author Galaxy
 * @description 针对表【auth_group(系统认证组)】的数据库操作Mapper
 * @createDate 2022-07-01 10:21:55
 * @Entity top.liheji.server.pojo.AuthGroup
 */
@Mapper
@CacheNamespace(implementation = MybatisPlusRedisCache.class)
public interface AuthGroupMapper extends BaseMapper<AuthGroup> {
}




