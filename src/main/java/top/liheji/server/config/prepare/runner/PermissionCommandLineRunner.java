package top.liheji.server.config.prepare.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import top.liheji.server.config.prepare.property.PermissionProperty;
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
    private PermissionProperty property;

    @Autowired
    private AuthPermissionService authPermissionService;

    @Override
    public void run(String... args) throws Exception {
        if (property.getCreate()) {
            authPermissionService.clear();
            for (PermissionProperty.Permission permission : property.getList()) {
                Class<?> clzz = permission.getClass();
                Field[] fields = clzz.getDeclaredFields();
                String suffix = property.getTableSuffix(permission);
                for (Field field : fields) {
                    field.setAccessible(true);
                    String key = field.getName();
                    Object value = field.get(permission);
                    if (!"table".equals(key) && !ObjectUtils.isEmpty(value)) {
                        authPermissionService.save(
                                new AuthPermission(key + suffix, permission.getTable(), (String) value)
                        );
                    }
                    field.setAccessible(false);
                }
            }

            log.info("权限信息初始化完成");
        }
    }
}
