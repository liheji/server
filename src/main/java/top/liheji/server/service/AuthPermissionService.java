package top.liheji.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.liheji.server.pojo.AuthPermission;
import top.liheji.server.util.page.PageUtils;

import java.util.Map;

/**
 * @author Galaxy
 * @description 针对表【auth_permission(系统认证权限)】的数据库操作Service
 * @createDate 2022-07-01 10:21:55
 */
public interface AuthPermissionService extends IService<AuthPermission> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 完全清理数据库
     */
    void clear();
}
