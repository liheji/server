package top.liheji.server.vo;

import lombok.Data;
import top.liheji.server.pojo.FileInfo;

import java.io.Serializable;
import java.util.Date;

/**
 * 上传信息
 *
 * @author Galaxy
 * @TableName server_upload_info
 */
@Data
public class UploadInfoVo implements Serializable {
    private static final long serialVersionUID = 1316180258924743209L;
    /**
     * 数据库字段
     */
    private Long id;

    private String fileName;

    private Date createTime;

    /**
     * 外键
     */
    private Long fileInfoId;

    private Long accountId;

    /**
     * 非数据库字段
     */
    private FileInfo fileInfo;
}