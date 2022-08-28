package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import top.liheji.server.service.AccountService;
import top.liheji.server.util.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 特殊通行Token
 *
 * @author : Galaxy
 * @TableName server_pass_token
 */
@TableName(value = "server_pass_token")
@Data
public class PassToken implements Serializable {
    /**
     * 数据库字段
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String tokenKey;

    private String tokenNote;

    private Date expireTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
    /**
     * 外键
     */
    private Integer accountId;

    /**
     * 非数据库字段
     */

    @TableField(exist = false)
    private Long expireStamp;

    @TableField(exist = false)
    @JsonIgnore
    private Account account;

    @TableField(exist = false)
    private static final long serialVersionUID = -8530090763294282982L;

    /**
     * 自定义方法
     */
    public PassToken() {
    }

    public PassToken(String tokenKey, Date expireTime) {
        this.tokenKey = tokenKey;
        this.expireTime = expireTime;
    }

    public void setExpireStamp(Long expireStamp) {
        if (expireStamp != null) {
            this.expireStamp = expireStamp;
            this.expireTime = new Date(this.expireStamp);
        }
    }

    public Account getAccount() {
        if (this.accountId != null) {
            this.account = BeanUtils.getBean(AccountService.class).getById(this.accountId);
        }
        return account;
    }
}