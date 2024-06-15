package top.yilee.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import top.yilee.server.service.AuthGroupPermissionsService;
import top.yilee.server.mapper.AuthGroupMapper;
import top.yilee.server.pojo.*;
import top.yilee.server.service.AuthAccountGroupsService;
import top.yilee.server.service.AuthGroupService;
import top.yilee.server.util.page.PageUtils;
import top.yilee.server.util.page.Query;
import top.yilee.server.vo.AuthGroupVo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Galaxy
 * @description 针对表【auth_group(系统认证组)】的数据库操作Service实现
 * @createDate 2022-07-01 10:21:55
 */
@Service("authGroupService")
public class AuthGroupServiceImpl extends ServiceImpl<AuthGroupMapper, AuthGroup>
        implements AuthGroupService {

    @Autowired
    private AuthAccountGroupsService authAccountGroupsService;

    @Autowired
    private AuthGroupPermissionsService authGroupPermissionsService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String param = (String) params.get("param");
        LambdaQueryWrapper<AuthGroup> queryWrapper = new LambdaQueryWrapper<>();
        if (!ObjectUtils.isEmpty(param)) {
            queryWrapper = queryWrapper.like(AuthGroup::getName, param)
                    .or()
                    .like(AuthGroup::getCodename, param);
        }
        IPage<AuthGroup> page = this.page(
                new Query<AuthGroup>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @CacheEvict(value = {"authorize"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveGroup(AuthGroupVo authGroupVo) {
        // 保存分组信息
        AuthGroup authGroup = new AuthGroup();
        BeanUtils.copyProperties(authGroupVo, authGroup);
        this.save(authGroup);

        List<Long> permissionIds = authGroupVo.getPermissionIds();
        // 保存分组对应的权限信息
        if (!ObjectUtils.isEmpty(permissionIds)) {
            List<AuthGroupPermissions> collect = permissionIds.stream().map(it -> {
                AuthGroupPermissions accountPermissions = new AuthGroupPermissions();
                accountPermissions.setGroupId(authGroup.getId());
                accountPermissions.setPermissionId(it);
                return accountPermissions;
            }).collect(Collectors.toList());
            authGroupPermissionsService.saveBatch(collect);
        }
    }

    @CacheEvict(value = {"authorize"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBatchGroup(Collection<Long> groupIds) {
        // 删除分组和权限的关联关系
        authGroupPermissionsService.remove(
                new LambdaQueryWrapper<AuthGroupPermissions>()
                        .in(AuthGroupPermissions::getGroupId, groupIds)
        );
        // 删除分组和账号的关联关系
        authAccountGroupsService.remove(
                new LambdaQueryWrapper<AuthAccountGroups>()
                        .in(AuthAccountGroups::getGroupId, groupIds)
        );
        // 删除分组
        this.removeByIds(groupIds);
    }

    @CacheEvict(value = {"authorize"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateGroup(AuthGroupVo authGroupVo) {
        AuthGroup authGroup = authGroupVo.toAuthGroup();
        // 删除不可更新的字段
        authGroupVo.setCodename(null);
        // 更新分组信息
        this.updateById(authGroup);

        List<AuthGroupPermissions> permissionList = authGroupVo.getGroupPermissionList();
        // 更新分组对应的权限信息
        if (!ObjectUtils.isEmpty(permissionList)) {
            // 删除原有的关联关系
            authGroupPermissionsService.remove(
                    new LambdaQueryWrapper<AuthGroupPermissions>()
                            .eq(AuthGroupPermissions::getGroupId, authGroup.getId())
            );
            // 保存新的关联关系
            authGroupPermissionsService.saveBatch(permissionList);
        }
    }
}




