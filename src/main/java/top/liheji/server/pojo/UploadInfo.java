package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import top.liheji.server.service.FileInfoService;
import top.liheji.server.util.BeanUtils;

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
public class UploadInfo implements Serializable {
    /**
     * 数据库字段
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String fileName;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 外键
     */
    private Integer fileInfoId;

    private Integer accountId;

    /**
     * 非数据库字段
     */
    @TableField(exist = false)
    private FileInfo fileInfo;

    @TableField(exist = false)
    private Account account;

    @TableField(exist = false)
    private static final long serialVersionUID = -1148089707419906713L;

    public UploadInfo() {
    }

    public UploadInfo(String fileName, Integer fileInfoId, Integer accountId) {
        this.fileName = fileName;
        this.fileInfoId = fileInfoId;
        this.accountId = accountId;
    }

    public FileInfo getFileInfo() {
        if (this.fileInfo == null && this.id != null) {
            this.fileInfo = BeanUtils.getBean(FileInfoService.class).getById(this.id);
        }
        return fileInfo;
    }
}