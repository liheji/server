package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.liheji.server.mapper.AuthGroupMapper;
import top.liheji.server.pojo.AuthGroup;
import top.liheji.server.service.AuthGroupService;

/**
* @author Galaxy
* @description 针对表【auth_group(系统认证组)】的数据库操作Service实现
* @createDate 2022-07-01 10:21:55
*/
@Service("authGroupService")
public class AuthGroupServiceImpl extends ServiceImpl<AuthGroupMapper, AuthGroup>
    implements AuthGroupService{

}




