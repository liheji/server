package top.liheji.server.config.remember.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import top.liheji.server.pojo.*;
import top.liheji.server.service.AccountService;
import top.liheji.server.service.AuthGroupService;
import top.liheji.server.service.AuthPermissionService;

import java.util.ArrayList;
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
public class CustomUserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthGroupService authGroupService;

    @Autowired
    private AuthPermissionService authPermissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountService.getOne(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getUsername, username)
                        .or()
                        .eq(Account::getMobile, username)
                        .or()
                        .eq(Account::getEmail, username)
        );

        if (username == null || "".equals(username) || account == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }

        Set<GrantedAuthority> auths = new HashSet<>();

        List<AuthGroup> groups = null;
        List<AuthPermission> permissions = null;

        if (account.getIsSuperuser()) {
            groups = authGroupService.list();
            permissions = authPermissionService.list();
        } else {
            groups = account.getAuthGroups();
            permissions = account.getAuthPermissions();
        }

        if (groups == null) {
            groups = new ArrayList<>();
        }
        if (permissions == null) {
            permissions = new ArrayList<>();
        }

        for (AuthGroup group : groups) {
            auths.add(new SimpleGrantedAuthority("ROLE_" + group.getCodename()));
            List<AuthPermission> permissions0 = group.getAuthPermissions();
            if (permissions0 != null) {
                for (AuthPermission permission : permissions0) {
                    auths.add(new SimpleGrantedAuthority(permission.getCodename()));
                }
            }
        }

        for (AuthPermission permission : permissions) {
            auths.add(new SimpleGrantedAuthority(permission.getCodename()));
        }

        return new User(account.getUsername(), account.getPassword(), account.getIsEnabled(),
                true, true, true, auths);
    }
}
