package top.yilee.server.service;

import top.yilee.server.pojo.LatexAccount;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Galaxy
 * @description 针对表【latex_account(Latex用户实体)】的数据库操作Service
 * @createDate 2022-10-17 13:55:41
 */
public interface LatexAccountService extends IService<LatexAccount> {

    /**
     * 获取 Latex 账号（不存在会注册）
     *
     * @param request 请求
     * @return Latex账号
     */
    LatexAccount getLatexAccount(HttpServletRequest request);

    /**
     * 更新 latex 账号的状态信息
     */
    void updateLatexAccountStatus();
}
