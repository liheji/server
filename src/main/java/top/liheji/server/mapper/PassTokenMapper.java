package top.liheji.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import top.liheji.server.config.cache.MybatisPlusRedisCache;
import top.liheji.server.pojo.PassToken;

/**
 * @author Galaxy
 * @description 针对表【server_pass_token(特殊通行Token)】的数据库操作Mapper
 * @createDate 2022-01-25 15:03:20
 * @Entity top.liheji.pojo.PassToken
 */
@Mapper
@CacheNamespace(implementation = MybatisPlusRedisCache.class)
public interface PassTokenMapper extends BaseMapper<PassToken> {

    /**
     * 查询Key
     *
     * @param tokenKey tokenKey
     * @return PassToken
     */
    PassToken selectTokenByKey(String tokenKey);
}




