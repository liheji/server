package top.liheji.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import top.liheji.server.config.cache.MybatisPlusRedisCache;
import top.liheji.server.pojo.PersistentDevices;

/**
* @author Galaxy
* @description 针对表【persistent_devices(登录设备)】的数据库操作Mapper
* @createDate 2022-01-25 22:17:14
* @Entity top.liheji.pojo.PersistentDevices
*/
@Mapper
@CacheNamespace(implementation = MybatisPlusRedisCache.class)
public interface PersistentDevicesMapper extends BaseMapper<PersistentDevices> {

}




