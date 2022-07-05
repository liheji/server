package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件信息
 *
 * @author Galaxy
 * @TableName server_file_attr
 */
@TableName(value = "server_file_attr")
@Data
public class FileAttr implements Serializable {
    /**
     * 数据库字段
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String fileName;

    private Long fileSize;

    private String fileHash;

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
    private Account account;

    @TableField(exist = false)
    private static final long serialVersionUID = -5714303279467640238L;
}