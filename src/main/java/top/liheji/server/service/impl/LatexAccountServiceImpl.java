package top.liheji.server.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import top.liheji.server.pojo.LatexAccount;
import top.liheji.server.service.LatexAccountService;
import top.liheji.server.mapper.LatexAccountMapper;
import org.springframework.stereotype.Service;
import top.liheji.server.vo.LatexRegisterVo;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Galaxy
 * @description 针对表【latex_account(Latex用户实体)】的数据库操作Service实现
 * @createDate 2022-10-17 13:55:41
 */
@Service("latexAccountService")
public class LatexAccountServiceImpl extends ServiceImpl<LatexAccountMapper, LatexAccount>
        implements LatexAccountService {
    private static final String REG_BY_ACCOUNT_ACTION = "https://reverse.latexlive.com:5002/api/Client/RegByAccount";

    @Autowired
    RestTemplate restTemplate;

    @Override
    public LatexAccount getLatexAccount(HttpServletRequest request) {
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

        LatexAccount result;

        List<LatexAccount> accountList = this.list(wrapper);
        if (ObjectUtils.isEmpty(accountList)) {
            result = registerByAccount(request);
            if (result == null) return null;
            // 保存账号
            result.setLastLogin(new Date());
            this.save(result);
        } else {
            result = accountList.get(0);
            // 跟新使用数据
            result.setLastLogin(new Date());
            this.updateById(result);
        }

        // 加密密码并返回
        result.setPassword(DigestUtil.md5Hex(result.getPassword()));

        return result;
    }

    @Override
    public void updateLatexAccountStatus() {
        List<LatexAccount> accountList = this.list();
        if (ObjectUtils.isEmpty(accountList)) {
            return;
        }
        List<Long> integerList = accountList.stream().map(LatexAccount::getId).collect(Collectors.toList());
        this.update(
                new LambdaUpdateWrapper<LatexAccount>()
                        .set(LatexAccount::getIsAvailable, true)
                        .in(LatexAccount::getId, integerList)
        );
    }

    private LatexAccount registerByAccount(HttpServletRequest req) {
        String userAgent = req.getHeader("User-Agent");
        if (ObjectUtils.isEmpty(userAgent)) {
            return null;
        }

        LatexRegisterVo latexVo = LatexRegisterVo.create(userAgent);

        // 设置请求头
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.add("User-Agent", userAgent);
        HttpEntity<LatexRegisterVo> entity = new HttpEntity<>(latexVo, requestHeaders);
        // 请求服务端添加玩家
        String dataStr = restTemplate.postForObject(REG_BY_ACCOUNT_ACTION, entity, String.class);
        JSONObject data = JSONObject.parseObject(dataStr);
        if (ObjectUtils.isEmpty(data) || data.getInteger("result").equals(1)) {
            return null;
        }
        return latexVo.toPojo();
    }
}




