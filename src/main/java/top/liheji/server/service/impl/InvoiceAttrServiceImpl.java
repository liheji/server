package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.liheji.server.mapper.InvoiceAttrMapper;
import top.liheji.server.pojo.InvoiceAttr;
import top.liheji.server.service.InvoiceAttrService;

/**
* @author Galaxy
* @description 针对表【server_invoice_attr(发票信息)】的数据库操作Service实现
* @createDate 2022-02-21 14:36:49
*/
@Service("invoiceAttrService")
public class InvoiceAttrServiceImpl extends ServiceImpl<InvoiceAttrMapper, InvoiceAttr>
    implements InvoiceAttrService{

}




