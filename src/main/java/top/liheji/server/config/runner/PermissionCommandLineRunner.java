package top.liheji.server.config.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.liheji.server.config.property.PermissionProperty;
import top.liheji.server.pojo.AuthPermission;
import top.liheji.server.service.AuthPermissionService;

import java.lang.reflect.Field;

/**
 * @author : Galaxy
 * @time : 2022/7/2 9:33
 * @create : IdeaJ
 * @project : serverPlus
 * @description :
 */
@Slf4j
@Order
@Component
public class PermissionCommandLineRunner implements CommandLineRunner {
    @Autowired
    private PermissionProperty permissionProperty;

    @Autowired
    private AuthPermissionService authPermissionService;

    @Override
    public void run(String... args) throws Exception {
        if (permissionProperty.getCreate() || authPermissionService.count() <= 0) {
            authPermissionService.clear();
            for (PermissionProperty.Permission permission : permissionProperty.getList()) {
                Class<?> clzz = permission.getClass();
                Field[] fields = clzz.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    String key = field.getName();
                    Object value = field.get(permission);
                    if (!"table".equals(key) && value != null) {
                        authPermissionService.save(new AuthPermission(
                                key + permission.getSubTable(),
                                permission.getTable(),
                                (String) value
                        ));
                    }
                    field.setAccessible(false);
                }
            }

            log.info("权限信息初始化完成");
        }
    }
}
