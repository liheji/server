package top.liheji.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.liheji.server.pojo.Account;

/**
* @author Galaxy
* @description 针对表【server_account(系统用户实体)】的数据库操作Mapper
* @createDate 2022-01-25 15:03:20
* @Entity top.liheji.pojo.Account
*/
@Mapper
public interface AccountMapper extends BaseMapper<Account> {

}




