package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.liheji.server.config.auth.AuthType;
import top.liheji.server.config.swagger.annotation.ApiIgnoreProperty;
import top.liheji.server.service.AccountService;
import top.liheji.server.util.BeanUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 第三方授权账户
 *
 * @author Galaxy
 * @TableName auth_account
 */
@TableName(value = "auth_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthAccount implements Serializable {

    /**
     * 数据库字段
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String openId;

    private String name;

    private String avatarUrl;

    private String authCode;

    /**
     * 外键
     */
    private Integer accountId;

    /**
     * 自定义方法
     */
    @TableField(exist = false)
    private static final long serialVersionUID = -6471191841884811150L;

    @ApiIgnoreProperty
    @TableField(exist = false)
    private String authType;

    @ApiIgnoreProperty
    @TableField(exist = false)
    @JsonIgnore
    private Account account;

    public AuthAccount(AuthType authType) {
        this.authCode = authType.getCode();
        this.authType = authType.getName();
    }

    public String getAuthType() {
        if (this.authType == null) {
            AuthType temp = AuthType.getByCode(this.authCode);
            if (temp != null) {
                return temp.getName();
            }
        }
        return this.authType;
    }

    public Account gainAccount() {
        if (this.accountId != null) {
            this.account = BeanUtils.getBean(AccountService.class).getById(this.accountId);
        }
        return this.account;
    }

    public Map<String, Object> objToMap() {
        Map<String, Object> hashMap = new HashMap<>(4);
        hashMap.put("openId", this.openId);
        hashMap.put("name", this.name);
        hashMap.put("avatarUrl", this.avatarUrl);
        hashMap.put("authCode", this.authCode);
        hashMap.put("authType", getAuthType());
        if (this.accountId != null) {
            Account account = gainAccount();
            hashMap.put("accountId", this.accountId);
            hashMap.put("username", account.getUsername());
        }
        return hashMap;
    }

    public void mapToObj(Map<String, Object> map) {
        this.openId = (String) map.get("openId");
        this.name = (String) map.get("name");
        this.avatarUrl = (String) map.get("avatarUrl");
        this.authCode = (String) map.get("authCode");
        this.authType = (String) map.get("authType");
        this.accountId = (Integer) map.get("accountId");
    }
}