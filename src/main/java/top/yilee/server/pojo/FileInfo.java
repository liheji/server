package top.yilee.server.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件信息
 *
 * @author Galaxy
 * @TableName server_file_info
 */
@TableName(value = "server_file_info")
@Data
@NoArgsConstructor
public class FileInfo implements Serializable {
    /**
     * 数据库字段
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;

    private Long fileSize;

    private String fileHash;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 非数据库字段
     */
    @TableField(exist = false)
    private static final long serialVersionUID = -5714303279467640238L;

    public FileInfo(String fileName, Long fileSize, String fileHash) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileHash = fileHash;
    }
}