package top.yilee.server.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.yilee.server.pojo.LatexAccount;
import top.yilee.server.service.LatexAccountService;
import top.yilee.server.util.R;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author : Galaxy
 * @time : 2021/11/25 23:46
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 实现用户操作相关功能接口
 */
@RestController
@RequestMapping("/latex")
public class LatexController {
    @Autowired
    private LatexAccountService latexAccountService;

    @GetMapping
    @PreAuthorize("hasAuthority('use_latexaccount')")
    public R obtainLatexAccount(HttpServletRequest request) {
        LatexAccount latexAccount = latexAccountService.getLatexAccount(request);
        if (latexAccount == null) {
            return R.error("无法注册，请检查 UserAgent");
        }
        return R.ok().put("account", latexAccount);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('use_latexaccount')")
    public R disableLatexAccount(@RequestBody Map<String, Object> params) {
        String username = params.get("username").toString();
        LambdaUpdateWrapper<LatexAccount> updateWrapper =
                new LambdaUpdateWrapper<LatexAccount>()
                        .eq(LatexAccount::getUsername, username)
                        .set(LatexAccount::getIsAvailable, false);
        latexAccountService.update(updateWrapper);
        return R.ok();
    }
}
