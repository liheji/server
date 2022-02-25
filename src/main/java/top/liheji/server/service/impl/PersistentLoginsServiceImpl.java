package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.liheji.server.mapper.PersistentLoginsMapper;
import top.liheji.server.pojo.PersistentLogins;
import top.liheji.server.service.PersistentLoginsService;

/**
* @author Galaxy
* @description 针对表【persistent_logins(登录cookie)】的数据库操作Service实现
* @createDate 2022-01-29 22:59:41
*/
@Service("persistentLoginsService")
public class PersistentLoginsServiceImpl extends ServiceImpl<PersistentLoginsMapper, PersistentLogins>
    implements PersistentLoginsService {

}




