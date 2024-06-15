package top.yilee.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.yilee.server.config.oauth.constant.OAuthType;
import top.yilee.server.constant.ServerConstant;
import top.yilee.server.pojo.*;
import top.yilee.server.service.AuthAccountService;
import top.yilee.server.util.R;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : Galaxy
 * @time : 2021/11/25 23:46
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 实现用户操作相关功能接口
 */
@RestController
@RequestMapping("/authAccount")
public class AuthAccountController {
    @Autowired
    AuthAccountService authAccountService;

    /**
     * 获取用户的第三方账号信息
     *
     * @return 第三方账号信息
     */
    @GetMapping
    public R queryAuthAccounts() {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        List<AuthAccount> authAccounts = authAccountService.getAuthAccountByAccountId(current.getId());
        Map<String, AuthAccount> authAccountMap = authAccounts.stream()
                .collect(Collectors.toMap(AuthAccount::getAuthCode, (obj) -> {
                    obj.setOpenId("");
                    return obj;
                }));
        OAuthType.available().forEach(it -> {
            String code = it.getCode();
            if (!authAccountMap.containsKey(code)) {
                AuthAccount obj = new AuthAccount();
                obj.setAuthCode(code);
                authAccountMap.put(code, obj);
            }
            authAccountMap.get(code).setAuthName(it.getName());
            authAccountMap.get(code).setAuthToken(null);
        });

        return R.ok().put("data", authAccountMap.values());
    }

    /**
     * 解绑第三方账户
     *
     * @param param 账户ID
     * @return 是否解绑成功
     */
    @DeleteMapping
    public R deleteAuthAccount(@RequestBody Map<String, Object> param) {
        String sId = param.get("id").toString();
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        authAccountService.remove(
                new LambdaQueryWrapper<AuthAccount>()
                        .eq(AuthAccount::getAccountId, current.getId())
                        .eq(AuthAccount::getId, Long.parseLong(sId))
        );
        return R.ok();
    }
}
