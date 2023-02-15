package top.liheji.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.AuthAccountPermissions;
import top.liheji.server.pojo.AuthPermission;

import java.util.List;

/**
 * @author Galaxy
 * @description 针对表【auth_account_permissions(系统认证权限分配)】的数据库操作Mapper
 * @createDate 2022-07-01 12:33:29
 * @Entity top.liheji.server.pojo.AuthAccountPermissions
 */
@Mapper
public interface AuthAccountPermissionsMapper extends BaseMapper<AuthAccountPermissions> {

    /**
     * 根据权限ID查询用户
     *
     * @param permissionId 权限ID
     * @return 用户列表
     */
    List<Account> selectAccountByPermissionId(Long permissionId);

    /**
     * 根据权限ID查询用户
     *
     * @param accountId 用户ID
     * @return 用户列表
     */
    List<AuthPermission> selectPermissionByAccountId(Long accountId);
}




