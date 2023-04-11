package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
    /**
     * 数据库字段
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String mobile;

    private String email;

    @TableField(fill = FieldFill.INSERT)
    private Boolean isEnabled;

    @TableField(fill = FieldFill.INSERT)
    private Boolean isSuperuser;

    @ApiModelProperty(hidden = true)
    private Date lastLogin;

    @ApiModelProperty(hidden = true)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(hidden = true)
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(hidden = true)
    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;

    @TableField(exist = false)
    private static final long serialVersionUID = -3386845044188835258L;
}