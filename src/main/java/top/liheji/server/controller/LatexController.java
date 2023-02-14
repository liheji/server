package top.liheji.server.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.liheji.server.pojo.LatexAccount;
import top.liheji.server.service.LatexAccountService;
import top.liheji.server.util.R;

import javax.servlet.http.HttpServletRequest;

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
    @PreAuthorize("hasAuthority('use_latex_account')")
    public R obtainLatexAccount(HttpServletRequest request) {
        LatexAccount latexAccount = latexAccountService.getLatexAccount(request);
        if (latexAccount == null) {
            return R.error("无法注册，请检查 UserAgent");
        }
        return R.ok().put("account", latexAccount);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('use_latex_account')")
    public R disableLatexAccount(String username) {
        LambdaUpdateWrapper<LatexAccount> updateWrapper =
                new LambdaUpdateWrapper<LatexAccount>()
                        .eq(LatexAccount::getUsername, username)
                        .set(LatexAccount::getIsAvailable, false);
        boolean isUpdate = latexAccountService.update(updateWrapper);
        return isUpdate ? R.ok() : R.error();
    }
}
