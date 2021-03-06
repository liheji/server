package top.liheji.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.PersistentDevices;
import top.liheji.server.service.PersistentDevicesService;
import top.liheji.server.service.PersistentLoginsService;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private PersistentLoginsService persistentLoginsService;

    @Autowired
    private PersistentDevicesService persistentDevicesService;

    @GetMapping
    public Map<String, Object> queryDevice(@RequestAttribute("series") String series,
                                           @RequestAttribute("account") Account current) {
        Map<String, Object> map = new HashMap<>(4);
        List<PersistentDevices> devicesList = persistentDevicesService.list(
                new LambdaQueryWrapper<PersistentDevices>()
                        .eq(PersistentDevices::getUsername, current.getUsername())
        );
        for (PersistentDevices devices : devicesList) {
            devices.setOther(series);
        }
        map.put("code", 0);
        map.put("msg", "获取设备成功");
        map.put("data", devicesList);
        map.put("total", devicesList.size());

        return map;
    }

    @PutMapping
    public Map<String, Object> changeDevice(String tp,
                                            HttpSession session,
                                            @RequestAttribute("series") String series,
                                            @RequestAttribute("account") Account current) {
        PersistentDevices persistentDevices = persistentDevicesService.getOne(
                new LambdaQueryWrapper<PersistentDevices>()
                        .eq(PersistentDevices::getType, tp)
                        .eq(PersistentDevices::getUsername, current.getUsername())
        );

        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 1);
        map.put("msg", "非法操作");
        if (persistentDevices != null) {
            //移除cookie
            persistentDevices.setOther(series);
            persistentLoginsService.removeById(persistentDevices.getSeries());
            persistentDevices.setSeries("");
            persistentDevicesService.update(
                    persistentDevices,
                    new LambdaQueryWrapper<PersistentDevices>()
                            .eq(PersistentDevices::getType, tp)
                            .eq(PersistentDevices::getUsername, current.getUsername())
            );

            map.put("code", 0);
            map.put("msg", "设备注销成功");
            if (persistentDevices.getIsCurrent()) {
                session.invalidate();
                map.put("msg", "本设备已注销");
            }
        }

        return map;
    }
}
