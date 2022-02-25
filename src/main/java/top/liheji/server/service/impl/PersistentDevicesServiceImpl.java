package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.liheji.server.mapper.PersistentDevicesMapper;
import top.liheji.server.pojo.PersistentDevices;
import top.liheji.server.service.PersistentDevicesService;

/**
 * @author Galaxy
 * @description 针对表【persistent_devices(登录设备)】的数据库操作Service实现
 * @createDate 2022-01-25 22:17:14
 */
@Service("persistentDevicesService")
public class PersistentDevicesServiceImpl extends ServiceImpl<PersistentDevicesMapper, PersistentDevices>
        implements PersistentDevicesService {

}
