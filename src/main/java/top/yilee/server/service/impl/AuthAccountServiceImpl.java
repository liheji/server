package top.yilee.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.yilee.server.service.AuthAccountService;
import top.yilee.server.mapper.AuthAccountMapper;
import top.yilee.server.pojo.AuthAccount;

import java.util.List;

/**
 * @author Galaxy
 * @description 针对表【auth_account(第三方授权账户)】的数据库操作Service实现
 * @createDate 2022-08-25 16:12:53
 */
@Service("authAccountService")
public class AuthAccountServiceImpl extends ServiceImpl<AuthAccountMapper, AuthAccount>
        implements AuthAccountService {

    @Override
    public List<AuthAccount> getAuthAccountByAccountId(Long accountId) {
        return this.list(
                new LambdaQueryWrapper<AuthAccount>()
                        .eq(AuthAccount::getAccountId, accountId)
        );
    }
}




