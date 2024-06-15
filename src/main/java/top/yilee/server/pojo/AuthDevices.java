package top.yilee.server.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 登录设备
 *
 * @author Galaxy
 * @TableName persistent_devices
 */
@TableName(value = "auth_devices")
@Data
@NoArgsConstructor
public class AuthDevices implements Serializable {
    /**
     * 数据库字段
     */
    @TableId
    private String type;

    private String username;

    private String ip;

    private String series;

    private String browser;

    private String operateSystem;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date lastUsed;


    /**
     * 自定义方法
     */
    public AuthDevices(String type, String browser, String operateSystem) {
        this.type = type;
        this.browser = browser;
        this.operateSystem = operateSystem;
    }

    @TableField(exist = false)
    private static final long serialVersionUID = -1457944217344577600L;
}