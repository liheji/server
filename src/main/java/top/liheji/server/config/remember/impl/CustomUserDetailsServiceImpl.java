package top.liheji.server.config.remember.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import top.liheji.server.pojo.Account;
import top.liheji.server.service.AccountService;

/**
 * @author : Galaxy
 * @time : 2022/1/24 13:05
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 设置 SpringSecurity 的用户加载，获取数据库中的用户
 */
@Service("userDetailsService")
public class CustomUserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private AccountService accountService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountService.getOne(
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
