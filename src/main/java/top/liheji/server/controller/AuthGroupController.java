package top.liheji.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.liheji.server.pojo.AuthAccountGroups;
import top.liheji.server.pojo.AuthGroup;
import top.liheji.server.pojo.AuthGroupPermissions;
import top.liheji.server.pojo.AuthPermission;
import top.liheji.server.service.AuthAccountGroupsService;
import top.liheji.server.service.AuthGroupPermissionsService;
import top.liheji.server.service.AuthGroupService;
import top.liheji.server.service.AuthPermissionService;

import java.util.*;

/**
 * @author : Galaxy
 * @time : 2021/11/6 10:12
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 实现通行认证Token相关接口
 */
@RestController
@RequestMapping("/authGroup")
public class AuthGroupController {

    @Autowired
    private AuthGroupService authGroupService;

    @Autowired
    private AuthPermissionService authPermissionService;

    @Autowired
    private AuthAccountGroupsService authAccountGroupsService;

    @Autowired
    private AuthGroupPermissionsService authGroupPermissionsService;

    @GetMapping
    @PreAuthorize("hasAuthority('view_group')")
    public Map<String, Object> queryAuthGroup(Integer page, Integer limit,
                                              @RequestParam(required = false, defaultValue = "") String param) {

        Page<AuthGroup> groupPage = authGroupService.page(
                new Page<>(page, limit),
                new LambdaQueryWrapper<AuthGroup>()
                        .like(AuthGroup::getName, param)
                        .or()
                        .like(AuthGroup::getCodename, param)
        );

        List<AuthGroup> groupList = groupPage.getRecords();
        Map<String, Object> map = new HashMap<>(5);
        map.put("code", 0);
        map.put("msg", "查询成功");
        map.put("count", groupList.size());
        map.put("total", groupPage.getTotal());
        map.put("data", groupList);
        return map;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('add_group')")
    public Map<String, Object> addAuthGroup(AuthGroup authGroup,
                                            @RequestParam(required = false) List<Integer> permissionIds) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("code", 1);
        map.put("msg", "数据错误");
        if (authGroupService.save(authGroup) &&
                authGroup.saveBatchPermission(permissionIds)) {
            map.put("code", 0);
            map.put("msg", "添加完成");
        }
        return map;
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('delete_group')")
    public Map<String, Object> deleteAuthGroup(@RequestParam List<Integer> groupIds) {
        boolean agDel = authAccountGroupsService.remove(
                new LambdaQueryWrapper<AuthAccountGroups>()
                        .in(AuthAccountGroups::getGroupId, groupIds)
        );
        boolean gpDel = authGroupPermissionsService.remove(
                new LambdaQueryWrapper<AuthGroupPermissions>()
                        .in(AuthGroupPermissions::getGroupId, groupIds)
        );

        Map<String, Object> map = new HashMap<>(4);
        if (agDel && gpDel) {
            map.put("code", 0);
            map.put("msg", "删除完成");
            map.put("count", authGroupService.getBaseMapper().deleteBatchIds(groupIds));
            map.put("total", groupIds.size());
        } else {
            map.put("code", 1);
            map.put("msg", "依赖无法删除");
        }

        return map;
    }

    @GetMapping("permissions")
    @PreAuthorize("hasAuthority('change_group')")
    public Map<String, Object> queryPermissions(Integer groupId) {
        Map<String, Object> map = new HashMap<>(3);

        map.put("code", 0);
        map.put("msg", "OK");
        AuthGroup authGroup = authGroupService.getById(groupId);

        List<AuthPermission> permissions = null;

        if (authGroup != null) {
            permissions = authGroup.getAuthPermissions();
        }

        if (permissions == null) {
            permissions = new ArrayList<>();
        }

        // 已授权的权限
        Set<Object> auths = new HashSet<>();
        for (AuthPermission permission : permissions) {
            auths.add(permission.getId());
        }

        Map<String, Object> data = new HashMap<>(2);
        data.put("permissionIds", auths);
        map.put("data", data);

        Map<String, Object> total = new HashMap<>(2);
        total.put("permissions", authPermissionService.list());
        map.put("total", total);

        return map;
    }

    @PutMapping
    @PreAuthorize("hasAuthority('change_group')")
    public Map<String, Object> changeAuthGroup(AuthGroup authGroup,
                                               @RequestParam(required = false) List<Integer> permissionIds) {
        authGroup.setCodename(null);
        Map<String, Object> map = new HashMap<>(2);
        map.put("code", 1);
        map.put("msg", "数据错误");
        if (authGroupService.updateById(authGroup) &&
                authGroup.saveBatchPermission(permissionIds)) {
            map.put("code", 0);
            map.put("msg", "更新完成");
        }
        return map;
    }
}
