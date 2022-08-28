package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.liheji.server.service.AuthGroupPermissionsService;
import top.liheji.server.util.BeanUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 系统认证权限
 *
 * @author Galaxy
 * @TableName auth_permission
 */
@TableName(value = "auth_permission")
@Data
@NoArgsConstructor
public class AuthPermission implements Serializable {
    /**
     * 数据库字段
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String codename;

    private String model;

    private String name;

    /**
     * 非数据库字段
     */

    @TableField(exist = false)
    @JsonIgnore
    private List<AuthGroup> authGroups;

    public AuthPermission(String codename, String model, String name) {
        this.codename = codename;
        this.model = model;
        this.name = name;
    }

    public List<AuthGroup> getAuthGroups() {
        if (this.id != null) {
            this.authGroups = BeanUtils.getBean(AuthGroupPermissionsService.class).selectGroupByPermissionId(this.id);
        }
        return authGroups;
    }

    @TableField(exist = false)
    private static final long serialVersionUID = -3178587641366542673L;
}