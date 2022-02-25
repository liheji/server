package top.liheji.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.liheji.server.pojo.PersistentLogins;

/**
* @author Galaxy
* @description 针对表【persistent_logins(登录cookie)】的数据库操作Mapper
* @createDate 2022-01-29 22:59:41
* @Entity top.liheji.pojo.PersistentLogins
*/
@Mapper
public interface PersistentLoginsMapper extends BaseMapper<PersistentLogins> {

}




