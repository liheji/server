package top.liheji.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.AuthAccountPermissions;
import top.liheji.server.pojo.AuthPermission;

import java.util.List;

/**
 * @author Galaxy
 * @description 针对表【auth_account_permissions(系统认证权限分配)】的数据库操作Service
 * @createDate 2022-07-01 12:33:29
 */
public interface AuthAccountPermissionsService extends IService<AuthAccountPermissions> {
    /**
     * 根据权限ID查询用户
     *
     * @param permissionId 权限ID
     * @return 用户列表
     */
    List<Account> getAccountByPermissionId(Long permissionId);

    /**
     * 根据权限ID查询用户
     *
     * @param accountId 用户ID
     * @return 用户列表
     */
    List<AuthPermission> getPermissionByAccountId(Long accountId);
}
