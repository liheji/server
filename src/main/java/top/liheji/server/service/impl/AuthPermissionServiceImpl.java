package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.liheji.server.mapper.AuthPermissionMapper;
import top.liheji.server.pojo.AuthPermission;
import top.liheji.server.service.AuthPermissionService;

/**
 * @author Galaxy
 * @description 针对表【auth_permission(系统认证权限)】的数据库操作Service实现
 * @createDate 2022-07-01 10:21:55
 */
@Service("authPermissionService")
public class AuthPermissionServiceImpl extends ServiceImpl<AuthPermissionMapper, AuthPermission>
        implements AuthPermissionService {

    @Override
    public void clear() {
        baseMapper.clear();
    }
}




