package top.yilee.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.yilee.server.pojo.AuthGroup;
import top.yilee.server.pojo.AuthPermission;
import top.yilee.server.pojo.AuthGroupPermissions;

import java.util.List;

/**
 * @author Galaxy
 * @description 针对表【auth_group_permissions(系统认证权限分配)】的数据库操作Mapper
 * @createDate 2022-07-01 10:21:55
 * @Entity top.yilee.server.pojo.AuthGroupPermissions
 */
@Mapper
public interface AuthGroupPermissionsMapper extends BaseMapper<AuthGroupPermissions> {

    /**
     * 根据分组（角色）ID查询权限
     *
     * @param groupId 分组（角色）ID
     * @return 权限列表
     */
    List<AuthPermission> selectPermissionByGroupId(Long groupId);


    /**
     * 根据权限ID查询分组
     *
     * @param permissionId 权限ID
     * @return 分组列表
     */
    List<AuthGroup> selectGroupByPermissionId(Long permissionId);
}




