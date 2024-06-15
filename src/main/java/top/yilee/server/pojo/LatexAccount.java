package top.yilee.server.pojo;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * Latex用户实体
 *
 * @author Galaxy
 * @TableName latex_account
 */
@Data
@TableName(value = "latex_account")
public class LatexAccount implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String equiptype;

    @TableField(fill = FieldFill.INSERT)
    private Boolean isAvailable;

    private Date lastLogin;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = -4917970750313947414L;
}