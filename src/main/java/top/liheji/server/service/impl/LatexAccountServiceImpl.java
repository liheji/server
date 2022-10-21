package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import top.liheji.server.pojo.LatexAccount;
import top.liheji.server.service.LatexAccountService;
import top.liheji.server.mapper.LatexAccountMapper;
import org.springframework.stereotype.Service;

/**
* @author Galaxy
* @description 针对表【latex_account(Latex用户实体)】的数据库操作Service实现
* @createDate 2022-10-17 13:55:41
*/
@Service("latexAccountService")
public class LatexAccountServiceImpl extends ServiceImpl<LatexAccountMapper, LatexAccount>
    implements LatexAccountService{

}




