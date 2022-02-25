package top.liheji.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.PassToken;
import top.liheji.server.service.PassTokenService;
import top.liheji.server.util.CypherUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Time : 2021/11/6 10:12
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : server
 * @Description :
 */
@RestController
@RequestMapping("/passToken")
public class PassTokenController {

    @Autowired
    private PassTokenService passTokenService;

    @GetMapping
    public Map<String, Object> queryPassToken(Integer page, Integer limit,
                                              @RequestAttribute("account") Account current,
                                              @RequestParam(required = false) String tokenNote) {
        Page<PassToken> tokenPage = passTokenService.page(
                new Page<>(page, limit),
                new QueryWrapper<PassToken>()
                        .like("token_note", tokenNote)
                        .eq("account_id", current.getId())
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
    public Map<String, Object> insertPassToken(PassToken passToken, @RequestAttribute("account") Account current) {
        passToken.setTokenKey(CypherUtils.genUuid());
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
    public Map<String, Object> deletePassToken(String id, @RequestAttribute("account") Account current) {
        String[] ids = id.split(",");
        int del = 0;
        for (String sp : ids) {
            if (
                    passTokenService.remove(new QueryWrapper<PassToken>()
                            .like("id", Integer.parseInt(sp))
                            .eq("account_id", current.getId()))
            ) {
                del++;
            }
        }

        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 0);
        map.put("msg", "删除完成");
        map.put("count", del);
        map.put("total", ids.length);
        return map;
    }

    @PutMapping
    public Map<String, Object> updatePassToken(PassToken passToken, @RequestAttribute("account") Account current) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("code", 1);
        map.put("msg", "数据错误");
        if (passTokenService.update(passToken,
                new QueryWrapper<PassToken>()
                        .like("id", passToken.getId())
                        .eq("account_id", current.getId()))
        ) {
            map.put("code", 0);
            map.put("msg", "更新完成");
        }
        return map;
    }

    //仅可以禁用

    @PutMapping("lock")
    public Map<String, Object> lockPassToken(String id, @RequestAttribute("account") Account current) {
        String[] ids = id.split(",");
        int del = 0;
        for (String sp : ids) {
            List<PassToken> tokenList = passTokenService.list(new QueryWrapper<PassToken>()
                    .like("id", Integer.parseInt(sp))
                    .eq("account_id", current.getId()));
            if (tokenList.size() > 0) {
                PassToken token = tokenList.get(0);
                token.setExpireTime(new Date(System.currentTimeMillis() - 1000));
                if (passTokenService.updateById(token)) {
                    del++;
                }
            }
        }
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 0);
        map.put("msg", "禁用完成");
        map.put("count", del);
        map.put("total", ids.length);
        return map;
    }
}
