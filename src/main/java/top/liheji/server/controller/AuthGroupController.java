package top.liheji.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.liheji.server.pojo.AuthPermission;
import top.liheji.server.service.AuthGroupPermissionsService;
import top.liheji.server.service.AuthGroupService;
import top.liheji.server.util.page.PageUtils;
import top.liheji.server.util.R;
import top.liheji.server.vo.AuthGroupVo;

import java.util.*;
import java.util.stream.Collectors;

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
    private AuthGroupPermissionsService authGroupPermissionsService;

    @GetMapping
    @PreAuthorize("hasAuthority('view_group')")
    public R queryAuthGroup(@RequestParam Map<String, Object> params) {
        PageUtils page = authGroupService.queryPage(params);
        return R.ok().put("page", page);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('add_group')")
    public R addAuthGroup(@RequestBody AuthGroupVo authGroupVo) {
        authGroupService.saveGroup(authGroupVo);
        return R.ok();
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('delete_group')")
    public R deleteAuthGroup(@RequestBody List<Long> groupIds) {
        authGroupService.deleteBatchGroup(groupIds);
        return R.ok();
    }

    @GetMapping("permissions/{groupId}")
    @PreAuthorize("hasAuthority('view_group')")
    public R queryPermissions(@PathVariable Long groupId) {
        List<AuthPermission> permissions = authGroupPermissionsService.getPermissionByGroupId(groupId);
        List<Long> longList = permissions.stream().map(AuthPermission::getId).collect(Collectors.toList());
        return R.ok().put("data", longList);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('change_group')")
    public R changeAuthGroup(@RequestBody AuthGroupVo authGroupVo) {
        authGroupService.updateGroup(authGroupVo);
        return R.ok();
    }
}
