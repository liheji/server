package top.liheji.server.config.auth.remember.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import top.liheji.server.pojo.*;
import top.liheji.server.service.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author : Galaxy
 * @time : 2022/1/24 13:05
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 设置 SpringSecurity 的用户加载，获取数据库中的用户
 */
@Service("userDetailsService")
public class RedisUserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthPermissionService authPermissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (ObjectUtils.isEmpty(username)) {
            throw new IllegalArgumentException("参数错误");
        }

        Account account = accountService.getOne(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getUsername, username)
                        .or()
                        .eq(Account::getMobile, username)
                        .or()
                        .eq(Account::getEmail, username)
        );

        if (ObjectUtils.isEmpty(account)) {
            throw new UsernameNotFoundException("用户不存在");
        }

        List<AuthPermission> permissions;

        // 是否管理员
        if (account.getIsSuperuser()) {
            permissions = authPermissionService.list();
        } else {
            permissions = accountService.getPermissionsByAccountId(account.getId());
        }

        Set<GrantedAuthority> auths = new HashSet<>();
        // 用户权限
        permissions.forEach(permission -> auths.add(new SimpleGrantedAuthority(permission.getCodename())));

        return new User(account.getUsername(), account.getPassword(), account.getIsEnabled(),
                true, true, true, auths);
    }
}
