package top.liheji.server.config.remember.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import top.liheji.server.mapper.AccountMapper;
import top.liheji.server.pojo.Account;

/**
 * @Time : 2022/1/24 13:05
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : mybatis-gen
 * @Description :
 */
@Service("userDetailsService")
public class CustomUserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private AccountMapper accountMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountMapper.selectOne(
                new QueryWrapper<Account>()
                        .eq("username", username)
                        .or()
                        .eq("email", username)
        );

        if (username == null || "".equals(username) || account == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }

        return new User(account.getUsername(), account.getPassword(), account.getIsEnabled(),
                true, true, true,
                AuthorityUtils.createAuthorityList("admin"));
    }
}
