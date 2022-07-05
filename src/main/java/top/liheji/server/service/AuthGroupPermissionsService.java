package top.liheji.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.liheji.server.pojo.AuthGroup;
import top.liheji.server.pojo.AuthGroupPermissions;
import top.liheji.server.pojo.AuthPermission;

import java.util.List;

/**
 * @author Galaxy
 * @description 针对表【auth_group_permissions(系统认证权限分配)】的数据库操作Service
 * @createDate 2022-07-01 10:21:55
 */
public interface AuthGroupPermissionsService extends IService<AuthGroupPermissions> {

    /**
     * 根据分组（角色）ID查询权限
     *
     * @param groupId 分组（角色）ID
     * @return 权限列表
     */
    List<AuthPermission> selectPermissionByGroupId(Integer groupId);

    /**
     * 根据权限ID查询分组
     *
     * @param permissionId 权限ID
     * @return 分组列表
     */
    List<AuthGroup> selectGroupByPermissionId(Integer permissionId);
}
