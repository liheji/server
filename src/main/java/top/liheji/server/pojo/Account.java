package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.liheji.server.config.swagger.annotation.ApiIgnoreProperty;
import top.liheji.server.config.auth.AuthType;
import top.liheji.server.service.AuthAccountGroupsService;
import top.liheji.server.service.AuthAccountPermissionsService;
import top.liheji.server.service.AuthAccountService;
import top.liheji.server.util.BeanUtils;

import java.io.Serializable;
import java.util.*;

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

    @ApiIgnoreProperty
    private Date lastLogin;

    @ApiIgnoreProperty
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiIgnoreProperty
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiIgnoreProperty
    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;

    /**
     * 非数据库字段
     */
    @ApiIgnoreProperty
    @TableField(exist = false)
    private List<AuthAccount> authAccounts;

    @ApiIgnoreProperty
    @TableField(exist = false)
    @JsonIgnore
    private List<AuthGroup> authGroups;

    @ApiIgnoreProperty
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

    public List<AuthAccount> getAuthAccounts() {
        if (this.id != null) {
            this.authAccounts = BeanUtils.getBean(AuthAccountService.class).list(
                    new LambdaQueryWrapper<AuthAccount>()
                            .eq(AuthAccount::getAccountId, this.id)
            );
            // 添加未绑定的数据
            Set<AuthType> authTypeSet = new HashSet<>(Arrays.asList(AuthType.values()));
            this.authAccounts.forEach((item) -> authTypeSet.remove(AuthType.getByCode(item.getAuthCode())));
            authTypeSet.forEach((item) -> {
                if (item.isEnabled()) {
                    this.authAccounts.add(new AuthAccount(item));
                }
            });
            // 用户排序
            this.authAccounts.sort(Comparator.comparingInt(o -> o.getAuthType().length()));
        }
        return authAccounts;
    }

    public List<AuthGroup> getAuthGroups() {
        if (this.id != null) {
            this.authGroups = BeanUtils.getBean(AuthAccountGroupsService.class).selectGroupByAccountId(this.id);
        }
        return authGroups;
    }

    public List<AuthPermission> getAuthPermissions() {
        if (this.id != null) {
            this.authPermissions = BeanUtils.getBean(AuthAccountPermissionsService.class).selectPermissionByAccountId(this.id);
        }
        return authPermissions;
    }

    public boolean saveBatchGroup(List<Integer> groupIds) {
        AuthAccountGroupsService accountGroupsService = BeanUtils.getBean(AuthAccountGroupsService.class);
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
        AuthAccountPermissionsService accountPermissionsService = BeanUtils.getBean(AuthAccountPermissionsService.class);
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