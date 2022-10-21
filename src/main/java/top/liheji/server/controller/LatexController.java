package top.liheji.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.liheji.server.pojo.LatexAccount;
import top.liheji.server.service.LatexAccountService;
import top.liheji.server.util.LatexUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author : Galaxy
 * @time : 2021/11/25 23:46
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 实现用户操作相关功能接口
 */
@Slf4j
@RestController
@RequestMapping("/latex")
public class LatexController {
    @Autowired
    private LatexAccountService latexAccountService;


    @GetMapping
    @PreAuthorize("hasAuthority('use_latex_account')")
    public Map<String, Object> obtainLatexAccount(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>(5);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        LambdaQueryWrapper<LatexAccount> wrapper =
                new LambdaQueryWrapper<LatexAccount>()
                        .lt(LatexAccount::getLastLogin, calendar.getTime())
                        .or()
                        .isNull(LatexAccount::getLastLogin)
                        .or((wpr) -> {
                            wpr.ge(LatexAccount::getLastLogin, calendar.getTime())
                                    .eq(LatexAccount::getIsAvailable, true);
                        })
                        .orderByAsc(LatexAccount::getId)
                        .last("limit 0,1");

        LatexAccount latexAccount = latexAccountService.getOne(wrapper);

        if (latexAccount == null) {
            latexAccount = LatexUtils.regByAccount(request);
            if (latexAccount == null) {
                map.put("code", 1);
                map.put("msg", "无法注册，请检查 UserAgent");
                return map;
            }
            // 保存账号
            latexAccount.setLastLogin(new Date());
            latexAccountService.save(latexAccount);
        } else {
            // 跟新使用数据
            latexAccountService.update(
                    new LambdaUpdateWrapper<LatexAccount>()
                            .eq(LatexAccount::getId, latexAccount.getId())
                            .set(LatexAccount::getLastLogin, new Date())
            );
        }

        // 加密密码并返回
        latexAccount.hashPassword();
        map.put("code", 0);
        map.put("msg", "OK");
        map.put("account", latexAccount);

        return map;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('use_latex_account')")
    public Map<String, Object> finishNotification(String username) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("code", 0);
        map.put("msg", "OK");

        LambdaUpdateWrapper<LatexAccount> updateWrapper =
                new LambdaUpdateWrapper<LatexAccount>()
                        .eq(LatexAccount::getUsername, username)
                        .set(LatexAccount::getIsAvailable, false);

        if (!latexAccountService.update(updateWrapper)) {
            map.put("msg", "Fail");
        }

        return map;
    }
}
