package top.liheji.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.AuthAccountGroups;
import top.liheji.server.pojo.AuthGroup;

import java.util.List;

/**
 * @author Galaxy
 * @description 针对表【auth_account_groups(用户分组)】的数据库操作Mapper
 * @createDate 2022-07-01 12:33:29
 * @Entity top.liheji.server.pojo.AuthAccountGroups
 */
@Mapper
public interface AuthAccountGroupsMapper extends BaseMapper<AuthAccountGroups> {

    /**
     * 根据分组（角色）ID查询用户
     *
     * @param groupId 分组（角色）ID
     * @return 用户列表
     */
    List<Account> selectAccountByGroupId(Long groupId);


    /**
     * 根据权限ID查询用户
     *
     * @param accountId 用户ID
     * @return 用户列表
     */
    List<AuthGroup> selectGroupByAccountId(Long accountId);
}




