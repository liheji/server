package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 上传信息
 *
 * @author Galaxy
 * @TableName server_upload_info
 */
@TableName(value = "server_upload_info")
@Data
@NoArgsConstructor
public class UploadInfo implements Serializable {
    /**
     * 数据库字段
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;

    @ApiModelProperty(hidden = true)
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 外键
     */
    private Long fileInfoId;

    private Long accountId;

    @TableField(exist = false)
    private static final long serialVersionUID = -1148089707419906713L;

    public UploadInfo(String fileName, Long fileInfoId, Long accountId) {
        this.fileName = fileName;
        this.fileInfoId = fileInfoId;
        this.accountId = accountId;
    }
}