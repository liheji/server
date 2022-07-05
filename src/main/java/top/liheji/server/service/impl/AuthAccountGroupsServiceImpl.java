package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.liheji.server.mapper.AuthAccountGroupsMapper;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.AuthAccountGroups;
import top.liheji.server.pojo.AuthGroup;
import top.liheji.server.service.AuthAccountGroupsService;

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
    public List<Account> selectAccountByGroupId(Integer groupId) {
        return baseMapper.selectAccountByGroupId(groupId);
    }

    @Override
    public List<AuthGroup> selectGroupByAccountId(Integer accountId) {
        return baseMapper.selectGroupByAccountId(accountId);
    }
}




