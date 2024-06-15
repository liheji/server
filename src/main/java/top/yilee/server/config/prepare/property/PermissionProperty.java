package top.yilee.server.config.prepare.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author : Galaxy
 * @time : 2022/7/2 8:04
 * @create : IdeaJ
 * @project : serverPlus
 * @description :
 */

@Data
@Component
@PropertySource("classpath:application.yml")
@ConfigurationProperties(prefix = "permission")
public class PermissionProperty {
    private String[] trimPrefix = {"server", "auth"};

    private Boolean create = false;
    private List<Permission> list;

    @Data
    public static class Permission {
        private String table;
        private String add;
        private String change;
        private String delete;
        private String view;

        private String all;

        // all 权限不包含下方的特殊权限
        private String use;
        private String download;

        public void setAll(String all) {
            this.add = "添加" + all;
            this.change = "修改" + all;
            this.delete = "删除" + all;
            this.view = "查看" + all;
            this.all = null;
        }
    }

    public String getTableSuffix(Permission permission) {
        String table = permission.getTable();
        for (String prefix : trimPrefix) {
            if (table.startsWith(prefix)) {
                table = table.replace(prefix, "");
            }
        }
        return "_" + table.replaceAll("_", "").toLowerCase();
    }
}

