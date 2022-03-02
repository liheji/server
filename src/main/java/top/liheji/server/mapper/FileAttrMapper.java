package top.liheji.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import top.liheji.server.config.cache.MybatisPlusRedisCache;
import top.liheji.server.pojo.FileAttr;

/**
* @author Galaxy
* @description 针对表【server_file_attr(文件信息)】的数据库操作Mapper
* @createDate 2022-01-25 15:03:20
* @Entity top.liheji.pojo.FileAttr
*/
@Mapper
@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface FileAttrMapper extends BaseMapper<FileAttr> {

}




