package top.yilee.server.vo;

import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;
import top.yilee.server.pojo.AuthGroup;
import top.yilee.server.pojo.AuthGroupPermissions;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统认证组
 *
 * @author Galaxy
 * @TableName auth_group
 */
@Data
public class AuthGroupVo implements Serializable {
    private static final long serialVersionUID = 532493229302098192L;
    /**
     * 数据库字段
     */
    private Long id;

    private String codename;

    private String name;

    private List<Long> permissionIds;

    public AuthGroup toAuthGroup() {
        AuthGroup authGroup = new AuthGroup();
        BeanUtils.copyProperties(this, authGroup);
        return authGroup;
    }

    public List<AuthGroupPermissions> getGroupPermissionList() {
        if (ObjectUtils.isEmpty(permissionIds)) {
            return null;
        }
        return permissionIds.stream().map(it -> {
            AuthGroupPermissions groupPermissions = new AuthGroupPermissions();
            groupPermissions.setGroupId(this.id);
            groupPermissions.setPermissionId(it);
            return groupPermissions;
        }).collect(Collectors.toList());
    }
}