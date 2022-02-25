package top.liheji.server.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 发票信息
 *
 * @TableName server_invoice_attr
 */
@TableName(value = "server_invoice_attr")
@Data
public class InvoiceAttr implements Serializable {
    /**
     * 数据库字段
     */
    @TableId
    private String code;

    private String num;

    private String checkCode;

    private Date date;

    private String amount;

    private String sellerName;

    private String purchaserName;

    private String commodityName;

    private String type;

    private String typeOrg;

    private String totalAmount;

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
    private Integer fileAttrId;

    private Integer accountId;

    /**
     * 非数据库字段
     */
    @TableField(exist = false)
    private FileAttr fileAttr;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}