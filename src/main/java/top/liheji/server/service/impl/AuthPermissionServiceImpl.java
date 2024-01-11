package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import top.liheji.server.service.AuthPermissionService;
import top.liheji.server.mapper.AuthPermissionMapper;
import top.liheji.server.pojo.AuthPermission;
import top.liheji.server.util.page.PageUtils;
import top.liheji.server.util.page.Query;

import java.util.Map;

/**
 * @author Galaxy
 * @description 针对表【auth_permission(系统认证权限)】的数据库操作Service实现
 * @createDate 2022-07-01 10:21:55
 */
@Service("authPermissionService")
public class AuthPermissionServiceImpl extends ServiceImpl<AuthPermissionMapper, AuthPermission>
        implements AuthPermissionService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String param = (String) params.get("param");
        LambdaQueryWrapper<AuthPermission> queryWrapper = new LambdaQueryWrapper<>();
        if (!ObjectUtils.isEmpty(param)) {
            queryWrapper = queryWrapper.like(AuthPermission::getName, param)
                    .or()
                    .like(AuthPermission::getModel, param)
                    .or()
                    .like(AuthPermission::getCodename, param);
        }
        IPage<AuthPermission> page = this.page(
                new Query<AuthPermission>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @CacheEvict(value = {"authorize"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void clear() {
        baseMapper.clear();
    }
}




