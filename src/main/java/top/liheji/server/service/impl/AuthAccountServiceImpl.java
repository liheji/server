package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.liheji.server.service.AuthAccountService;
import top.liheji.server.mapper.AuthAccountMapper;
import top.liheji.server.pojo.AuthAccount;

/**
* @author Galaxy
* @description 针对表【auth_account(第三方授权账户)】的数据库操作Service实现
* @createDate 2022-08-25 16:12:53
*/
@Service("authAccountService")
public class AuthAccountServiceImpl extends ServiceImpl<AuthAccountMapper, AuthAccount>
    implements AuthAccountService {

}




