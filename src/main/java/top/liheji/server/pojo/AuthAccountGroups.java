package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户分组
 *
 * @author Galaxy
 * @TableName auth_account_groups
 */
@TableName(value = "auth_account_groups")
@Data
@NoArgsConstructor
public class AuthAccountGroups implements Serializable {
    /**
     * 数据库字段
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 外键
     */
    private Long accountId;

    private Long groupId;

    /**
     * 非数据库字段
     */
    public AuthAccountGroups(Long accountId, Long groupId) {
        this.accountId = accountId;
        this.groupId = groupId;
    }

    @TableField(exist = false)
    private static final long serialVersionUID = -6394641918659513159L;
}