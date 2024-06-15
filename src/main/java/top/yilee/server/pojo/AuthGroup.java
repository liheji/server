package top.yilee.server.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
    private Long id;

    private String codename;

    private String name;

    @TableField(exist = false)
    private static final long serialVersionUID = -9112066944505029694L;
}