package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.liheji.server.mapper.AccountMapper;
import top.liheji.server.pojo.Account;
import top.liheji.server.service.AccountService;

/**
* @author Galaxy
* @description 针对表【server_account(系统用户实体)】的数据库操作Service实现
* @createDate 2022-01-25 15:03:20
*/
@Service("accountService")
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account>
    implements AccountService{

}




