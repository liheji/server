package top.yilee.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.yilee.server.config.auth.remember.impl.RedisTokenRepositoryImpl;
import top.yilee.server.mapper.AuthDevicesMapper;
import top.yilee.server.pojo.Account;
import top.yilee.server.pojo.AuthDevices;
import top.yilee.server.service.AccountService;
import top.yilee.server.service.AuthDevicesService;

/**
 * @author Galaxy
 * @description 针对表【persistent_devices(登录设备)】的数据库操作Service实现
 * @createDate 2022-01-25 22:17:14
 */
@Service("authDevicesService")
public class AuthDevicesServiceImpl extends ServiceImpl<AuthDevicesMapper, AuthDevices>
        implements AuthDevicesService {

    @Autowired
    AccountService accountService;

    @Autowired
    private RedisTokenRepositoryImpl tokenRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void invalidateDevice(AuthDevices authDevices) {
        //移除cookie
        tokenRepository.removeUserTokens(authDevices.getUsername(), authDevices.getType());
        authDevices.setSeries("");
        this.updateById(authDevices);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Account updateLoginInfo(AuthDevices device) {
        LambdaQueryWrapper<AuthDevices> queryWrapper = new LambdaQueryWrapper<AuthDevices>()
                .eq(AuthDevices::getType, device.getType())
                .eq(AuthDevices::getUsername, device.getUsername());

        // 先查询是否存在
        AuthDevices existingDevice = this.getOne(queryWrapper);
        if (existingDevice != null) {
            this.update(device, queryWrapper);
        } else {
            this.save(device);
        }

        // 更新登录日期
        accountService.update(
                new LambdaUpdateWrapper<Account>()
                        .set(Account::getLastLogin, device.getLastUsed())
                        .eq(Account::getUsername, device.getUsername())
        );

        return accountService.getOne(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getUsername, device.getUsername())
        );
    }
}
