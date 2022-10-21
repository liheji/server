package top.liheji.server.pojo;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import top.liheji.server.config.swagger.annotation.ApiIgnoreProperty;
import top.liheji.server.util.Algorithms;
import top.liheji.server.util.CypherUtils;

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
    private Integer id;

    private String username;

    private String password;

    private String tel;

    private String mail;

    private String equiptype;

    @TableField(fill = FieldFill.INSERT)
    private Boolean isAvailable;

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

    @TableField(exist = false)
    private static final long serialVersionUID = -4917970750313947414L;


    public void hashPassword() {
        if (this.password == null || "".equals(this.password.trim())) {
            throw new NullPointerException("密码为空");
        }
        this.password = CypherUtils.encodeToHash(this.password, Algorithms.MD5).toLowerCase();
    }

    public String registerForm() {
        JSONObject json = new JSONObject();
        json.put("username", this.username);
        json.put("tel", this.tel);
        json.put("mail", this.mail);
        json.put("password", CypherUtils.encodeToHash(this.password, Algorithms.MD5).toLowerCase());
        json.put("equiptype", this.equiptype);
        return json.toJSONString();
    }

    public String loginForm() {
        JSONObject json = new JSONObject();
        json.put("loginname", this.username);
        json.put("password", CypherUtils.encodeToHash(this.password, Algorithms.MD5).toLowerCase());
        return json.toJSONString();
    }
}