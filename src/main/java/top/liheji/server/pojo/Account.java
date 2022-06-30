package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 系统用户实体
 *
 * @author Galaxy
 * @TableName server_account
 */
@Data
@TableName(value = "server_account")
public class Account implements Serializable {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w\\-]+@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,})$");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^((\\+86)|(86))?1\\d{10}$");
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 数据库字段
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;

    private String password;

    private String mobile;

    private String email;

    @TableField(fill = FieldFill.INSERT)
    private Boolean isEnabled;

    private Date lastLogin;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;

    /**
     * 非数据库字段
     */
    @TableField(exist = false)
    private static final long serialVersionUID = 3386845044188835258L;

    /**
     * 自定义方法
     */
    public Account() {
    }

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
        this.bcryptPassword();
    }

    public Account(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void bcryptPassword() {
        if (this.password == null || "".equals(this.password.trim())) {
            throw new NullPointerException("密码为空");
        }
        this.password = new BCryptPasswordEncoder().encode(this.password);
    }

    public void clearOthers() {
        this.username = null;
        this.lastLogin = null;
        this.updateTime = null;
        this.createTime = null;
    }

    public boolean notMatchEmail() {
        if (this.email == null || "".equals(this.email.trim())) {
            return false;
        }
        return !EMAIL_PATTERN.matcher(this.email.trim()).find();
    }

    public boolean notMatchMobile() {
        if (this.mobile == null || "".equals(this.mobile.trim())) {
            return false;
        }
        return !MOBILE_PATTERN.matcher(this.mobile.trim()).find();
    }

    public boolean matchPassword(String password) {
        return this.password != null && ENCODER.matches(this.password, password);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Account account = (Account) o;
        return Objects.equals(username, account.username);
    }
}