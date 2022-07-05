package top.liheji.server.config.property;

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
    private Boolean create = false;
    private List<Permission> list;

    @Data
    public static class Permission {
        private String table;
        private String add;
        private String change;
        private String delete;
        private String view;
        private String use;
        private String download;
        private String all;

        public String getSubTable() {
            return table.substring(table.indexOf("_"));
        }

        public void setAll(String all) {
            this.add = "添加" + all;
            this.change = "修改" + all;
            this.delete = "删除" + all;
            this.view = "查看" + all;
            this.all = null;
        }
    }
}

