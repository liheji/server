package top.liheji.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.liheji.server.constant.ServerConstant;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.AuthDevices;
import top.liheji.server.service.AuthDevicesService;
import top.liheji.server.util.R;
import top.liheji.server.vo.AuthDevicesVo;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : Galaxy
 * @time : 2021/10/29 22:19
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 实现登录设备相关接口
 */
@RestController
@RequestMapping("/device")
public class DeviceController {
    @Autowired
    private AuthDevicesService authDevicesService;

    @GetMapping
    public R queryDevice() {
        String series = ServerConstant.LOCAL_SERIES.get();
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        List<AuthDevices> devicesList = authDevicesService.list(
                new LambdaQueryWrapper<AuthDevices>()
                        .eq(AuthDevices::getUsername, current.getUsername())
        );

        List<AuthDevicesVo> resultData = devicesList.stream().map(it -> {
            AuthDevicesVo vo = new AuthDevicesVo();
            BeanUtils.copyProperties(it, vo);
            vo.setOther(series);
            return vo;
        }).collect(Collectors.toList());

        return R.ok().put("data", resultData);
    }

    @PutMapping
    public R changeDevice(@RequestBody Map<String, Object> params, HttpSession session) {
        String type = params.get("type").toString();
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        AuthDevices persistentDevices = authDevicesService.getOne(
                new LambdaQueryWrapper<AuthDevices>()
                        .eq(AuthDevices::getType, type)
                        .eq(AuthDevices::getUsername, current.getUsername())
        );

        if (persistentDevices == null) {
            return R.error("非法操作");
        }

        authDevicesService.invalidateDevice(persistentDevices);
        if (persistentDevices.getSeries().equals(ServerConstant.LOCAL_SERIES.get())) {
            session.invalidate();
        }

        return R.ok();
    }
}
