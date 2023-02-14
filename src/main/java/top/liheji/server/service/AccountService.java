package top.liheji.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.AuthGroup;
import top.liheji.server.pojo.AuthPermission;
import top.liheji.server.util.page.PageUtils;
import top.liheji.server.vo.AccountVo;

import java.util.List;
import java.util.Map;

/**
 * @author Galaxy
 * @description 针对表【server_account(系统用户实体)】的数据库操作Service
 * @createDate 2022-01-25 15:03:20
 */
public interface AccountService extends IService<Account> {
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 更新用户信息
     *
     * @param accountVo 账号信息
     */
    void updateAccount(AccountVo accountVo);

    /**
     * 获取用户分组
     *
     * @param accountId 用户ID
     * @return 分组列表
     */
    List<AuthGroup> getGroupsByAccountId(Long accountId);

    /**
     * 获取用户权限
     *
     * @param accountId 用户ID
     * @return 权限列表
     */
    List<AuthPermission> getPermissionsByAccountId(Long accountId);
}
