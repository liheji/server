package top.liheji.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.liheji.server.pojo.AuthPermission;
import top.liheji.server.service.AuthPermissionService;

import java.util.HashMap;
import java.util.List;
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
    public Map<String, Object> queryAuthPermission(Integer page, Integer limit,
                                                   @RequestParam(required = false, defaultValue = "") String param) {
        Page<AuthPermission> permissionPage = authPermissionService.page(
                new Page<>(page, limit),
                new LambdaQueryWrapper<AuthPermission>()
                        .like(AuthPermission::getName, param)
                        .or()
                        .like(AuthPermission::getModel, param)
                        .or()
                        .like(AuthPermission::getCodename, param)
        );

        List<AuthPermission> permissionList = permissionPage.getRecords();
        Map<String, Object> map = new HashMap<>(5);
        map.put("code", 0);
        map.put("msg", "查询成功");
        map.put("count", permissionList.size());
        map.put("total", permissionPage.getTotal());
        map.put("data", permissionList);
        return map;
    }

    @PutMapping
    @PreAuthorize("hasAuthority('change_permission')")
    public Map<String, Object> changeAuthPermission(AuthPermission authPermission) {
        authPermission.setModel(null);
        authPermission.setCodename(null);
        Map<String, Object> map = new HashMap<>(2);
        map.put("code", 1);
        map.put("msg", "数据错误");
        if (authPermissionService.updateById(authPermission)) {
            map.put("code", 0);
            map.put("msg", "更新完成");
        }
        return map;
    }
}
