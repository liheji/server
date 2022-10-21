package top.liheji.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.liheji.server.pojo.Account;
import top.liheji.server.service.AccountService;
import top.liheji.server.service.CaptchaService;
import top.liheji.server.util.FileUtils;
import top.liheji.server.util.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * @author : Galaxy
 * @time : 2022/2/4 10:38
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 实现登录后非实体相关接口
 */
@Slf4j
@RestController
@RequestMapping("/before")
public class BeforeController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private CaptchaService captchaService;

    @PostMapping("forget")
    public Map<String, Object> forget(String username, String key, String password) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 1);
        map.put("msg", "用户不存在");

        Account account = accountService.getOne(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getUsername, username)
                        .or()
                        .eq(Account::getEmail, username)
                        .or()
                        .eq(Account::getMobile, username)
        );
        if (account != null) {
            if (captchaService.checkCaptcha(key)) {
                account.setPassword(password);
                account.bcryptPassword();
                if (accountService.saveOrUpdate(account)) {
                    map.put("code", 0);
                    map.put("msg", "修改完成");
                }
            } else {
                map.put("code", 1);
                map.put("msg", "校验码错误");
            }
        }
        return map;
    }

    @PostMapping("register")
    public Map<String, Object> register(Account account, String licence) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 1);
        map.put("msg", "授权码错误");

        if (captchaService.checkSecret(null, licence)) {
            account.bcryptPassword();
            if (accountService.save(account)) {
                map.put("code", 0);
                map.put("msg", "注册成功");
            } else {
                map.put("code", 1);
                map.put("msg", "系统错误");
            }
        }

        return map;
    }

    @GetMapping("imageCaptcha")
    public Map<String, Object> imgCaptcha(@RequestParam(required = false, defaultValue = "100") Integer width,
                                          @RequestParam(required = false, defaultValue = "38") Integer height) throws Exception {
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 0);
        map.put("msg", "获取成功");
        map.put("data", captchaService.genImgCaptcha(4, width, height));

        return map;
    }

    @GetMapping("sendCaptcha")
    public Map<String, Object> sendCaptcha(String receiver, String property) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 1);
        map.put("msg", "用户不存在");

        Account account = accountService.getOne(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getUsername, receiver)
                        .or()
                        .eq(Account::getEmail, receiver)
                        .or()
                        .eq(Account::getMobile, receiver)
        );
        if (account != null) {
            switch (property) {
                case "email":
                    captchaService.sendEmailCaptcha(receiver);
                    map.put("code", 0);
                    map.put("msg", "发送成功");
                    break;
                case "mobile":
                    // 发送手机验证码
                    //  captchaService.sendMobileCaptcha(receiver);
                    map.put("code", 1);
                    map.put("msg", "暂不支持手机号验证");
                    break;
                default:
                    throw new RuntimeException("所选类型不存在");
            }
        }

        return map;
    }

    @GetMapping("uniqueCheck")
    public Map<String, Object> uniqueCheck(String param) {
        long count = accountService.count(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getUsername, param)
                        .or()
                        .eq(Account::getMobile, param)
                        .or()
                        .eq(Account::getEmail, param)
        );

        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 0);
        map.put("msg", "OK");
        map.put("result", count > 0);
        return map;
    }

    @PostMapping("cdn")
    public void cdnPost(@RequestParam("file") MultipartFile file, @RequestHeader("AUTH-TOKEN") String xToken,
                       HttpServletResponse resp) throws Exception {
        final String basePath = "/usr/local/cdn";
        final String passToken = "217d65b63a22401a810a0442492a5f4e";
        if (!passToken.equals(xToken)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String originName = file.getOriginalFilename();

        File save = new File(basePath, MediaType.guessMediaType(originName).split("/")[0]);
        if (save.isFile() || !save.exists()) {
            save.mkdirs();
        }
        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + FileUtils.splitText(originName)[1];
        save = new File(save, fileName);

        //为读取文件提供流通道
        @Cleanup InputStream in = file.getInputStream();
        @Cleanup OutputStream os = new FileOutputStream(save);

        int num;
        byte[] bytes = new byte[1024];
        while ((num = in.read(bytes)) != -1) {
            os.write(bytes, 0, num);
        }

        resp.setContentType("application/json;charset=utf-8");
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 0);
        map.put("msg", "OK");
        map.put("url", save.getAbsolutePath().replace(basePath, ""));

        PrintWriter out = resp.getWriter();
        out.write(JSONObject.toJSONString(map));
        out.flush();
        out.close();
    }

    @DeleteMapping("cdn")
    public void cdnDelete(@RequestHeader("AUTH-TOKEN") String xToken, String filename,
                          HttpServletResponse resp) throws Exception {
        final String basePath = "/usr/local/cdn";
        final String passToken = "217d65b63a22401a810a0442492a5f4e";
        if (!passToken.equals(xToken)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        File save = new File(basePath, MediaType.guessMediaType(filename).split("/")[0]);
        if (save.isFile() || !save.exists()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        save = new File(save, filename);
        if (!save.exists()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        //为读取文件提供流通道
        resp.setContentType("application/json;charset=utf-8");
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 0);
        map.put("msg", "OK");
        if (!save.delete()) {
            map.put("code", 1);
            map.put("msg", "FAIL");
        }

        PrintWriter out = resp.getWriter();
        out.write(JSONObject.toJSONString(map));
        out.flush();
        out.close();
    }
}
