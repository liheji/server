package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.liheji.server.mapper.PassTokenMapper;
import top.liheji.server.pojo.PassToken;
import top.liheji.server.service.PassTokenService;

/**
 * @author Galaxy
 * @description 针对表【server_pass_token(特殊通行Token)】的数据库操作Service实现
 * @createDate 2022-01-25 15:03:20
 */
@Service("passTokenService")
public class PassTokenServiceImpl extends ServiceImpl<PassTokenMapper, PassToken>
        implements PassTokenService {

    @Override
    public PassToken selectTokenByKey(String tokenKey) {
        return baseMapper.selectTokenByKey(tokenKey);
    }
}




