package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import top.liheji.server.mapper.AccountMapper;
import top.liheji.server.pojo.*;
import top.liheji.server.service.*;
import top.liheji.server.util.page.PageUtils;
import top.liheji.server.util.page.Query;
import top.liheji.server.vo.AccountVo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Galaxy
 * @description 针对表【server_account(系统用户实体)】的数据库操作Service实现
 * @createDate 2022-01-25 15:03:20
 */
@Service("accountService")
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account>
        implements AccountService {

    @Autowired
    AuthGroupService authGroupService;

    @Autowired
    AuthPermissionService authPermissionService;

    @Autowired
    AuthAccountGroupsService authAccountGroupsService;

    @Autowired
    AuthAccountPermissionsService authAccountPermissionsService;

    @Autowired
    AuthGroupPermissionsService authGroupPermissionsService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String username = (String) params.get("username");
        String isEnabled = (String) params.get("isEnabled");
        LambdaQueryWrapper<Account> queryWrapper = new LambdaQueryWrapper<Account>();
        if (!ObjectUtils.isEmpty(isEnabled)) {
            queryWrapper = queryWrapper.like(Account::getUsername, username);
        }
        if (!ObjectUtils.isEmpty(isEnabled)) {
            queryWrapper = queryWrapper.eq(Account::getIsEnabled, Boolean.parseBoolean(isEnabled));
        }
        IPage<Account> page = this.page(
                new Query<Account>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }


    @CacheEvict(value = {"authorize"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAccount(AccountVo accountVo) {
        Account account = accountVo.toAccount();
        // 清空不可更新数据
        account.setUsername(null);
        // 复制属性
        this.updateById(account);

        List<AuthAccountGroups> groupList = accountVo.getAccountGroupList();
        // 删除源数据
        authAccountGroupsService.remove(
                new LambdaQueryWrapper<AuthAccountGroups>()
                        .eq(AuthAccountGroups::getAccountId, accountVo.getId())
        );
        if (!ObjectUtils.isEmpty(groupList)) {
            // 保存新数据
            authAccountGroupsService.saveBatch(groupList);
        }

        List<AuthAccountPermissions> permissionList = accountVo.getAccountPermissionList();
        // 删除源数据
        authAccountPermissionsService.remove(
                new LambdaQueryWrapper<AuthAccountPermissions>()
                        .eq(AuthAccountPermissions::getAccountId, accountVo.getId())
        );
        if (!ObjectUtils.isEmpty(permissionList)) {

            if (!ObjectUtils.isEmpty(accountVo.getGroupIds())) {
                // 排除GROUP拥有的权限
                Set<Long> longSet = authGroupPermissionsService.list(
                        new LambdaQueryWrapper<AuthGroupPermissions>()
                                .in(AuthGroupPermissions::getGroupId, accountVo.getGroupIds())
                ).stream().map(AuthGroupPermissions::getPermissionId).collect(Collectors.toSet());
                permissionList = permissionList.stream().filter(
                        authAccountPermissions -> !longSet.contains(authAccountPermissions.getPermissionId())
                ).collect(Collectors.toList());
            }
            // 保存新数据
            authAccountPermissionsService.saveBatch(permissionList);
        }
    }

    @Override
    @Cacheable(value = {"authorize"}, keyGenerator = "keyGenerator", sync = true)
    public List<AuthGroup> getGroupsByAccountId(Long accountId) {
        Account account = this.getById(accountId);
        if (account.getIsSuperuser()) {
            return new ArrayList<>(0);
        }
        return authAccountGroupsService.getGroupByAccountId(accountId);
    }

    @Override
    @Cacheable(value = {"authorize"}, keyGenerator = "keyGenerator", sync = true)
    public List<AuthPermission> getPermissionsByAccountId(Long accountId) {
        Account account = this.getById(accountId);
        if (account.getIsSuperuser()) {
            return new ArrayList<>(0);
        }
        List<AuthPermission> accountPermissions = authAccountPermissionsService.getPermissionByAccountId(accountId);
        // 合并并通过Map(ID属性作为key)去重
        Map<Long, AuthPermission> permissionMap = accountPermissions.stream().collect(Collectors.toMap(AuthPermission::getId, it -> it));
        authAccountGroupsService
                .getGroupByAccountId(accountId)
                .forEach(it -> {
                    authGroupPermissionsService
                            .getPermissionByGroupId(it.getId())
                            .forEach(permission -> {
                                permissionMap.putIfAbsent(permission.getId(), permission);
                            });
                });

        return new ArrayList<>(permissionMap.values());
    }
}




