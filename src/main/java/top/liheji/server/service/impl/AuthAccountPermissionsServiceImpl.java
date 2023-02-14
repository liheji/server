package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.liheji.server.service.AuthAccountPermissionsService;
import top.liheji.server.mapper.AuthAccountPermissionsMapper;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.AuthAccountPermissions;
import top.liheji.server.pojo.AuthPermission;

import java.util.List;

/**
 * @author Galaxy
 * @description 针对表【auth_account_permissions(系统认证权限分配)】的数据库操作Service实现
 * @createDate 2022-07-01 12:33:29
 */
@Service("authAccountPermissionsService")
public class AuthAccountPermissionsServiceImpl extends ServiceImpl<AuthAccountPermissionsMapper, AuthAccountPermissions>
        implements AuthAccountPermissionsService {

    @Override
    public List<Account> getAccountByPermissionId(Long permissionId) {
        return baseMapper.selectAccountByPermissionId(permissionId);
    }

    @Override
    public List<AuthPermission> getPermissionByAccountId(Long accountId) {
        return baseMapper.selectPermissionByAccountId(accountId);
    }
}




