package top.liheji.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import top.liheji.server.config.cache.MybatisPlusRedisCache;
import top.liheji.server.pojo.InvoiceAttr;

/**
* @author Galaxy
* @description 针对表【server_invoice_attr(发票信息)】的数据库操作Mapper
* @createDate 2022-02-21 14:36:49
* @Entity top.liheji.server.pojo.InvoiceAttr
*/
@Mapper
@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface InvoiceAttrMapper extends BaseMapper<InvoiceAttr> {

}
