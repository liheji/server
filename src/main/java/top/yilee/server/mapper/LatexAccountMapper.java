package top.yilee.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.yilee.server.pojo.LatexAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Galaxy
* @description 针对表【latex_account(Latex用户实体)】的数据库操作Mapper
* @createDate 2022-10-17 13:55:41
* @Entity top.yilee.server.pojo.LatexAccount
*/
@Mapper
public interface LatexAccountMapper extends BaseMapper<LatexAccount> {

}




