package top.liheji.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.PassToken;
import top.liheji.server.service.PassTokenService;
import top.liheji.server.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : Galaxy
 * @time : 2021/11/6 10:12
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 实现通行认证Token相关接口
 */
@RestController
@RequestMapping("/passToken")
public class PassTokenController {

    @Autowired
    private PassTokenService passTokenService;

    @GetMapping
    @PreAuthorize("hasAuthority('view_pass_token')")
    public Map<String, Object> queryPassToken(Integer page, Integer limit,
                                              @RequestAttribute("account") Account current,
                                              @RequestParam(required = false, defaultValue = "") String tokenNote) {
        Page<PassToken> tokenPage = passTokenService.page(
                new Page<>(page, limit),
                new LambdaQueryWrapper<PassToken>()
                        .like(PassToken::getTokenNote, tokenNote)
                        .eq(PassToken::getAccountId, current.getId())
        );

        List<PassToken> tokenList = tokenPage.getRecords();
        Map<String, Object> map = new HashMap<>(5);
        map.put("code", 0);
        map.put("msg", "查询成功");
        map.put("count", tokenList.size());
        map.put("total", tokenPage.getTotal());
        map.put("data", tokenList);
        return map;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('add_pass_token')")
    public Map<String, Object> insertPassToken(PassToken passToken, @RequestAttribute("account") Account current) {
        passToken.setTokenKey(StringUtils.genUuidWithoutLine());
        passToken.setAccountId(current.getId());

        Map<String, Object> map = new HashMap<>(2);
        map.put("code", 1);
        map.put("msg", "数据错误");
        if (passTokenService.save(passToken)) {
            map.put("code", 0);
            map.put("msg", "添加完成");
        }
        return map;
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('delete_pass_token')")
    public Map<String, Object> deletePassToken(@RequestParam List<Integer> tokenIds,
                                               @RequestAttribute("account") Account current) {

        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 0);
        map.put("msg", "删除完成");
        map.put("count", passTokenService.getBaseMapper().delete(
                new LambdaQueryWrapper<PassToken>()
                        .eq(PassToken::getAccountId, current.getId())
                        .in(PassToken::getId, tokenIds)
        ));
        map.put("total", tokenIds.size());
        return map;
    }

    @PutMapping
    @PreAuthorize("hasAuthority('change_pass_token')")
    public Map<String, Object> updatePassToken(PassToken passToken, @RequestAttribute("account") Account current) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("code", 1);
        map.put("msg", "数据错误");
        if (passTokenService.update(passToken,
                new LambdaQueryWrapper<PassToken>()
                        .eq(PassToken::getId, passToken.getId())
                        .eq(PassToken::getAccountId, current.getId()))
        ) {
            map.put("code", 0);
            map.put("msg", "更新完成");
        }
        return map;
    }

    //仅可以禁用

    @PutMapping("lock")
    @PreAuthorize("hasAuthority('change_pass_token')")
    public Map<String, Object> lockPassToken(@RequestParam List<Integer> tokenIds,
                                             @RequestAttribute("account") Account current) {

        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 0);
        map.put("msg", "禁用完成");
        map.put("count", passTokenService.getBaseMapper().update(null,
                new LambdaUpdateWrapper<PassToken>()
                        .set(PassToken::getExpireTime, new Date(System.currentTimeMillis() - 1000))
                        .eq(PassToken::getAccountId, current.getId())
                        .in(PassToken::getId, tokenIds)
        ));
        map.put("total", tokenIds.size());
        return map;
    }
}
