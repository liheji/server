package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
    private Long id;

    private String codename;

    private String model;

    private String name;

    public AuthPermission(String codename, String model, String name) {
        this.codename = codename;
        this.model = model;
        this.name = name;
    }

    @TableField(exist = false)
    private static final long serialVersionUID = -3178587641366542673L;
}