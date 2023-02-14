package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.liheji.server.service.AuthGroupPermissionsService;
import top.liheji.server.mapper.AuthGroupPermissionsMapper;
import top.liheji.server.pojo.AuthGroup;
import top.liheji.server.pojo.AuthGroupPermissions;
import top.liheji.server.pojo.AuthPermission;

import java.util.List;

/**
 * @author Galaxy
 * @description 针对表【auth_group_permissions(系统认证权限分配)】的数据库操作Service实现
 * @createDate 2022-07-01 10:21:55
 */
@Service("authGroupPermissionsService")
public class AuthGroupPermissionsServiceImpl extends ServiceImpl<AuthGroupPermissionsMapper, AuthGroupPermissions>
        implements AuthGroupPermissionsService {

    @Override
    public List<AuthPermission> getPermissionByGroupId(Long groupId) {
        return baseMapper.selectPermissionByGroupId(groupId);
    }

    @Override
    public List<AuthGroup> getGroupByPermissionId(Long permissionId) {
        return baseMapper.selectGroupByPermissionId(permissionId);
    }
}




