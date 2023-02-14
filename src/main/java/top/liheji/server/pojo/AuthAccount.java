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
 * 第三方授权账户
 *
 * @author Galaxy
 * @TableName auth_account
 */
@TableName(value = "auth_account")
@Data
@NoArgsConstructor
public class AuthAccount implements Serializable {

    /**
     * 数据库字段
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String openId;

    private String name;

    private String avatarUrl;

    private String authCode;

    /**
     * 外键
     */
    private Long accountId;

    /**
     * 自定义方法
     */
    @TableField(exist = false)
    private static final long serialVersionUID = -6471191841884811150L;
}