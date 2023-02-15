package top.liheji.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.liheji.server.pojo.AuthDevices;

/**
* @author Galaxy
* @description 针对表【persistent_devices(登录设备)】的数据库操作Mapper
* @createDate 2022-01-25 22:17:14
* @Entity top.liheji.pojo.PersistentDevices
*/
@Mapper
public interface AuthDevicesMapper extends BaseMapper<AuthDevices> {

}




