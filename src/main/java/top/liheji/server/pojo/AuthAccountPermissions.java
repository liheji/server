package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 系统认证权限分配
 *
 * @author Galaxy
 * @TableName auth_account_permissions
 */
@TableName(value = "auth_account_permissions")
@Data
@NoArgsConstructor
public class AuthAccountPermissions implements Serializable {
    /**
     * 数据库字段
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 外键
     */
    private Integer accountId;

    private Integer permissionId;

    /**
     * 非数据库字段
     */
    public AuthAccountPermissions(Integer accountId, Integer permissionId) {
        this.accountId = accountId;
        this.permissionId = permissionId;
    }

    @TableField(exist = false)
    private static final long serialVersionUID = -1214979430466063704L;
}