package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 登录设备
 *
 * @author Galaxy
 * @TableName persistent_devices
 */
@TableName(value = "persistent_devices")
@Data
public class PersistentDevices implements Serializable {
    /**
     * 数据库字段
     */
    @TableId
    private String type;

    private String username;

    private String series;

    private String browser;

    private String operateSystem;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date lastUsed;

    /**
     * 非数据库字段
     */
    @TableField(exist = false)
    private Boolean isValid;

    @TableField(exist = false)
    private Boolean isCurrent;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 自定义方法
     */
    public PersistentDevices() {
    }

    public PersistentDevices(String type, String browser, String operateSystem) {
        this.type = type;
        this.browser = browser;
        this.operateSystem = operateSystem;
    }

    public void setOther(String currentSeries) {
        this.isValid = (this.series != null && !this.series.trim().isEmpty());
        this.isCurrent = this.series != null && this.series.equals(currentSeries);
    }
}