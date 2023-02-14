package top.liheji.server.vo;

import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;
import top.liheji.server.pojo.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统用户实体
 *
 * @author Galaxy
 * @TableName server_account
 */
@Data
public class AccountVo implements Serializable {
    private static final long serialVersionUID = 7934634304698629291L;
    /**
     * 数据库字段
     */
    private Long id;

    private String username;

    private String password;

    private String mobile;

    private String email;

    private Boolean isEnabled;

    private Boolean isSuperuser;

    private Boolean resetPassword;

    /**
     * 非数据库字段
     */
    List<Long> groupIds;
    List<Long> permissionIds;

    public Account toAccount() {
        Account account = new Account();
        // 复制属性
        BeanUtils.copyProperties(this, account);
        return account;
    }

    public List<AuthAccountGroups> getAccountGroupList() {
        if (ObjectUtils.isEmpty(groupIds)) {
            return null;
        }

        return groupIds.stream().map(it -> {
            AuthAccountGroups accountGroups = new AuthAccountGroups();
            accountGroups.setAccountId(this.id);
            accountGroups.setGroupId(it);
            return accountGroups;
        }).collect(Collectors.toList());
    }

    public List<AuthAccountPermissions> getAccountPermissionList() {
        if (ObjectUtils.isEmpty(permissionIds)) {
            return null;
        }
        return permissionIds.stream().map(it -> {
            AuthAccountPermissions accountPermissions = new AuthAccountPermissions();
            accountPermissions.setAccountId(this.id);
            accountPermissions.setPermissionId(it);
            return accountPermissions;
        }).collect(Collectors.toList());
    }
}