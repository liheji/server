package top.liheji.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.liheji.server.pojo.PassToken;

/**
 * @author Galaxy
 * @description 针对表【server_pass_token(特殊通行Token)】的数据库操作Service
 * @createDate 2022-01-25 15:03:20
 */
public interface PassTokenService extends IService<PassToken> {
    /**
     * 查询Key
     *
     * @param tokenKey tokenKey
     * @return PassToken
     */
    PassToken selectTokenByKey(String tokenKey);
}
