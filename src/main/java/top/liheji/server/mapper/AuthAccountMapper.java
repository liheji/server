package top.liheji.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.liheji.server.pojo.AuthAccount;

/**
* @author Galaxy
* @description 针对表【auth_account(第三方授权账户)】的数据库操作Mapper
* @createDate 2022-08-25 16:12:53
* @Entity top.liheji.server.pojo.AuthAccount
*/
@Mapper
public interface AuthAccountMapper extends BaseMapper<AuthAccount> {

}
