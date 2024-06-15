package top.yilee.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.yilee.server.pojo.AuthPermission;

/**
 * @author Galaxy
 * @description 针对表【auth_permission(系统认证权限)】的数据库操作Mapper
 * @createDate 2022-07-01 10:21:55
 * @Entity top.yilee.server.pojo.AuthPermission
 */
@Mapper
public interface AuthPermissionMapper extends BaseMapper<AuthPermission> {

    /**
     * 完全清理数据库
     */
    void clear();
}




