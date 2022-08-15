package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.liheji.server.service.AuthAccountGroupsService;
import top.liheji.server.service.AuthAccountPermissionsService;
import top.liheji.server.util.SpringBeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 系统用户实体
 *
 * @author Galaxy
 * @TableName server_account
 */
@Data
@TableName(value = "server_account")
public class Account implements Serializable {
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 数据库字段
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;

    private String password;

    private String mobile;

    private String email;

    @TableField(fill = FieldFill.INSERT)
    private Boolean isEnabled;

    @TableField(fill = FieldFill.INSERT)
    private Boolean isSuperuser;

    private Date lastLogin;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;

    /**
     * 非数据库字段
     */
    @TableField(exist = false)
    @JsonIgnore
    private List<AuthGroup> authGroups;

    @TableField(exist = false)
    @JsonIgnore
    private List<AuthPermission> authPermissions;

    @TableField(exist = false)
    private static final long serialVersionUID = -3386845044188835258L;

    /**
     * 自定义方法
     */
    public Account() {
    }

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
        this.bcryptPassword();
    }

    public Account(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public List<AuthGroup> getAuthGroups() {
        if (this.authGroups == null && this.id != null) {
            this.authGroups = SpringBeanUtils.getBean(AuthAccountGroupsService.class).selectGroupByAccountId(this.id);
        }
        return authGroups;
    }

    public List<AuthPermission> getAuthPermissions() {
        if (this.authPermissions == null && this.id != null) {
            this.authPermissions = SpringBeanUtils.getBean(AuthAccountPermissionsService.class).selectPermissionByAccountId(this.id);
        }
        return authPermissions;
    }

    public boolean saveBatchGroup(List<Integer> groupIds) {
        AuthAccountGroupsService accountGroupsService = SpringBeanUtils.getBean(AuthAccountGroupsService.class);
        accountGroupsService.remove(
                new LambdaQueryWrapper<AuthAccountGroups>()
                        .eq(AuthAccountGroups::getAccountId, this.id)
        );
        if (groupIds == null) {
            return true;
        }
        List<AuthAccountGroups> accountGroups = new ArrayList<>();
        for (Integer groupId : groupIds) {
            accountGroups.add(new AuthAccountGroups(this.id, groupId));
        }
        return accountGroupsService.saveBatch(accountGroups);
    }

    public boolean saveBatchPermission(List<Integer> permissionIds) {
        AuthAccountPermissionsService accountPermissionsService = SpringBeanUtils.getBean(AuthAccountPermissionsService.class);
        accountPermissionsService.remove(
                new LambdaQueryWrapper<AuthAccountPermissions>()
                        .eq(AuthAccountPermissions::getAccountId, this.id)
        );
        if (permissionIds == null) {
            return true;
        }
        List<AuthAccountPermissions> accountPermissions = new ArrayList<>();
        for (Integer permissionId : permissionIds) {
            accountPermissions.add(new AuthAccountPermissions(this.id, permissionId));
        }
        return accountPermissionsService.saveBatch(accountPermissions);
    }

    public void bcryptPassword() {
        if (this.password == null || "".equals(this.password.trim())) {
            throw new NullPointerException("密码为空");
        }
        this.password = new BCryptPasswordEncoder().encode(this.password);
    }

    public void clearOtherExcludeBoolean() {
        this.username = null;
        this.lastLogin = null;
        this.updateTime = null;
        this.createTime = null;
        this.version = null;
    }

    public boolean matchPassword(String password) {
        return this.password != null && ENCODER.matches(password, this.password);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Account account = (Account) o;
        return Objects.equals(username, account.username);
    }

    public static String bcryptPassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }
}