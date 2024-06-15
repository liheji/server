package top.yilee.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.yilee.server.mapper.AuthAccountGroupsMapper;
import top.yilee.server.pojo.Account;
import top.yilee.server.pojo.AuthAccountGroups;
import top.yilee.server.pojo.AuthGroup;
import top.yilee.server.service.AuthAccountGroupsService;

import java.util.List;

/**
 * @author Galaxy
 * @description 针对表【auth_account_groups(用户分组)】的数据库操作Service实现
 * @createDate 2022-07-01 12:33:29
 */
@Service("authAccountGroupsService")
public class AuthAccountGroupsServiceImpl extends ServiceImpl<AuthAccountGroupsMapper, AuthAccountGroups>
        implements AuthAccountGroupsService {

    @Override
    public List<Account> getAccountByGroupId(Long groupId) {
        return baseMapper.selectAccountByGroupId(groupId);
    }

    @Override
    public List<AuthGroup> getGroupByAccountId(Long accountId) {
        return baseMapper.selectGroupByAccountId(accountId);
    }
}




