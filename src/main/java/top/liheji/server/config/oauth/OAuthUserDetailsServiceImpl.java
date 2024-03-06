package top.liheji.server.config.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import top.dcenter.ums.security.core.oauth.service.UmsUserDetailsService;

import java.io.IOException;
import java.util.List;

@Service
public class OAuthUserDetailsServiceImpl implements UmsUserDetailsService {
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public UserDetails loadUserByUserId(String userId) throws UsernameNotFoundException {
        return userDetailsService.loadUserByUsername(userId);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userDetailsService.loadUserByUsername(username);
    }

    @Override
    public List<Boolean> existedByUsernames(String... usernames) throws IOException {
        return null;
    }
}
