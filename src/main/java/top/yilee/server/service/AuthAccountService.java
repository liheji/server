package top.yilee.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.yilee.server.pojo.AuthAccount;

import java.util.List;

/**
 * @author Galaxy
 * @description 针对表【auth_account(第三方授权账户)】的数据库操作Service
 * @createDate 2022-08-25 16:12:53
 */
public interface AuthAccountService extends IService<AuthAccount> {
    /**
     * 获取绑定的第三方账号
     *
     * @param accountId 用户ID
     * @return 第三方账号列表
     */
    List<AuthAccount> getAuthAccountByAccountId(Long accountId);
}
