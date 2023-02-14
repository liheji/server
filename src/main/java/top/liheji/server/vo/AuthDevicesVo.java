package top.liheji.server.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 登录设备
 *
 * @author Galaxy
 * @TableName persistent_devices
 */
@Data
public class AuthDevicesVo implements Serializable {
    private static final long serialVersionUID = -7703578188783614004L;
    /**
     * 数据库字段
     */
    private String type;

    private String username;

    private String ip;

    private String series;

    private String browser;

    private String operateSystem;

    private Date lastUsed;

    private Boolean isValid;

    private Boolean isCurrent;

    /**
     * 自定义方法
     *
     * @param currentSeries
     */
    public void setOther(String currentSeries) {
        this.isValid = (this.series != null && !this.series.trim().isEmpty());
        this.isCurrent = this.series != null && this.series.equals(currentSeries);
    }
}