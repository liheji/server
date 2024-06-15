package top.yilee.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.yilee.server.pojo.AuthPermission;
import top.yilee.server.service.AuthPermissionService;
import top.yilee.server.util.page.PageUtils;
import top.yilee.server.util.R;

import java.util.Map;

/**
 * @author : Galaxy
 * @time : 2021/11/6 10:12
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 实现通行认证Token相关接口
 */
@RestController
@RequestMapping("/authPermission")
public class AuthPermissionController {

    @Autowired
    private AuthPermissionService authPermissionService;

    @GetMapping
    @PreAuthorize("hasAuthority('view_permission')")
    public R queryAuthPermission(@RequestParam Map<String, Object> params) {
        PageUtils page = authPermissionService.queryPage(params);
        return R.ok().put("page", page);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('change_permission')")
    public R changeAuthPermission(@RequestBody AuthPermission authPermission) {
        authPermission.setModel(null);
        authPermission.setCodename(null);
        authPermissionService.updateById(authPermission);
        return R.ok();
    }
}
