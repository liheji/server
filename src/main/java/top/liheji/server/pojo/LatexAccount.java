package top.liheji.server.pojo;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import top.liheji.server.config.swagger.annotation.ApiIgnoreProperty;

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

    @ApiIgnoreProperty
    private Date lastLogin;

    @ApiIgnoreProperty
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = -4917970750313947414L;
}