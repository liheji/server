package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.liheji.server.service.AuthGroupPermissionsService;
import top.liheji.server.util.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统认证组
 *
 * @author Galaxy
 * @TableName auth_group
 */
@TableName(value = "auth_group")
@Data
@NoArgsConstructor
public class AuthGroup implements Serializable {
    /**
     * 数据库字段
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String codename;

    private String name;

    /**
     * 非数据库字段
     */
    @TableField(exist = false)
    @JsonIgnore
    private List<AuthPermission> authPermissions;

    public AuthGroup(String codename, String name) {
        this.codename = codename;
        this.name = name;
    }

    public List<AuthPermission> getAuthPermissions() {
        if (this.authPermissions == null && this.id != null) {
            this.authPermissions = BeanUtils.getBean(AuthGroupPermissionsService.class).selectPermissionByGroupId(this.id);
        }
        return authPermissions;
    }

    public boolean saveBatchPermission(List<Integer> permissionIds) {
        AuthGroupPermissionsService groupPermissionsService = BeanUtils.getBean(AuthGroupPermissionsService.class);
        groupPermissionsService.remove(
                new LambdaQueryWrapper<AuthGroupPermissions>()
                        .eq(AuthGroupPermissions::getGroupId, this.id)
        );
        if (permissionIds == null) {
            return true;
        }
        List<AuthGroupPermissions> accountPermissions = new ArrayList<>();
        for (Integer permissionId : permissionIds) {
            accountPermissions.add(new AuthGroupPermissions(this.id, permissionId));
        }
        return groupPermissionsService.saveBatch(accountPermissions);
    }

    @TableField(exist = false)
    private static final long serialVersionUID = -9112066944505029694L;
}