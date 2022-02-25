package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 登录cookie
 *
 * @author Galaxy
 * @TableName persistent_logins
 */
@TableName(value = "persistent_logins")
@Data
public class PersistentLogins implements Serializable {
    /**
     * 数据库字段
     */
    @TableId
    private String series;

    private String username;

    private String token;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date lastUsed;

    /**
     * 非数据库字段
     */
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}