package top.liheji.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.AuthDevices;

/**
* @author Galaxy
* @description 针对表【persistent_devices(登录设备)】的数据库操作Service
* @createDate 2022-01-25 22:17:14
*/
public interface AuthDevicesService extends IService<AuthDevices> {

    void invalidateDevice(AuthDevices authDevices);

    Account updateLoginInfo(AuthDevices device);
}
