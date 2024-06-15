package top.yilee.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.yilee.server.constant.ServerConstant;
import top.yilee.server.util.page.PageUtils;
import top.yilee.server.constant.CaptchaTypeEnum;
import top.yilee.server.constant.MediaType;
import top.yilee.server.pojo.Account;
import top.yilee.server.pojo.FileInfo;
import top.yilee.server.pojo.UploadInfo;
import top.yilee.server.service.CaptchaService;
import top.yilee.server.service.UploadInfoService;
import top.yilee.server.util.*;
import top.yilee.server.vo.FileCheckVo;
import top.yilee.server.vo.UploadInfoVo;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;


/**
 * @author : Galaxy
 * @time : 2021/10/29 22:19
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 实现文件相关接口
 */
@Slf4j
@RestController
@RequestMapping("/uploadInfo")
public class UploadInfoController {
    @Autowired
    CaptchaService captchaService;

    @Autowired
    UploadInfoService uploadInfoService;


    @GetMapping
    @PreAuthorize("hasAuthority('view_uploadinfo')")
    public R queryUploadInfo(@RequestParam Map<String, Object> params) {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        PageUtils page = uploadInfoService.queryPage(params, current.getId());
        return R.ok().put("page", page);
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('delete_uploadinfo')")
    public R deleteUploadInfo(@RequestBody List<Long> fileIds) {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        uploadInfoService.getBaseMapper().delete(
                new LambdaQueryWrapper<UploadInfo>()
                        .eq(UploadInfo::getAccountId, current.getId())
                        .in(UploadInfo::getId, fileIds)
        );
        return R.ok();
    }

    @PostMapping("verify")
    @PreAuthorize("hasAuthority('add_uploadinfo')")
    public R checkUploadInfo(@RequestBody FileCheckVo checkVo) {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        UploadInfoVo result = uploadInfoService.getUploadInfoVo(checkVo, current.getId());
        if (result == null) {
            return R.error().put("key", captchaService.genCaptcha(current.getUsername(), CaptchaTypeEnum.GENERAL_SECRET));
        }
        return R.ok().put("data", result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('add_uploadinfo')")
    public R addUploadInfo(@RequestParam("file") MultipartFile file,
                           @RequestHeader(value = "UPLOAD-TOKEN", defaultValue = "") String uToken) throws Exception {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        if (!captchaService.checkCaptcha(current.getUsername(), uToken)) {
            return R.error("上传码错误");
        }

        //上传文件
        FileInfo fileInfo = FileUtils.uploadFile(file);
        UploadInfo uploadInfo = new UploadInfo(file.getOriginalFilename(), fileInfo.getId(), current.getId());
        UploadInfoVo result = uploadInfoService.saveUploadInfo(fileInfo, uploadInfo);
        return R.ok().put("data", result);
    }

    @GetMapping("download/{param:.+}")
    @PreAuthorize("hasAuthority('download_uploadinfo')")
    public void downloadFile(@PathVariable String param,
                             @RequestHeader(value = "Range", required = false) String range,
                             HttpServletResponse resp) throws IOException {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        UploadInfoVo uploadInfoVo = uploadInfoService.getUploadInfoVo(param, current.getId());
        final String uploadFileName = (uploadInfoVo == null) ? param.trim() : uploadInfoVo.getFileName();
        final String fileName = (uploadInfoVo == null) ? param.trim() : uploadInfoVo.getFileInfo().getFileName();
        //get方式提交的
        File file = FileUtils.staticFile("uploads", fileName);
        if (!file.exists()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long fileSize = file.length();
        long[] pos = StrUtils.parseRange(range, fileSize);
        long contentLength = pos[1] - pos[0] + 1;

        //通知客户端以下载的方式打开
        resp.setContentLengthLong(contentLength);
        resp.setHeader("Accept-Ranges", "bytes");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(uploadFileName, "UTF-8"));
        resp.setHeader("Content-Range", String.format("bytes %d-%d/%d", pos[0], pos[1], fileSize));
        resp.setHeader("Content-Type", "application/octet-stream");
        resp.setHeader("Last-Modified", new Date(file.lastModified()).toString());

        if (contentLength != fileSize) {
            resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        }

        //获取输出流
        @Cleanup OutputStream out = resp.getOutputStream();
        FileUtils.writePos(file, out, pos[0], contentLength);
    }

    @GetMapping("preview/{param:.+}")
    @PreAuthorize("hasAuthority('view_uploadinfo')")
    public void previewFile(@PathVariable String param,
                            HttpServletResponse resp) throws Exception {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        UploadInfoVo uploadInfoVo = uploadInfoService.getUploadInfoVo(param, current.getId());
        final String uploadFileName = (uploadInfoVo == null) ? param.trim() : uploadInfoVo.getFileName();
        final String fileName = (uploadInfoVo == null) ? param.trim() : uploadInfoVo.getFileInfo().getFileName();
        //get方式提交的
        File file = FileUtils.staticFile("uploads", fileName);
        if (!file.exists()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        MediaType mediaType = MediaType.guessMediaTypeClass(file.getName());
        if (!mediaType.isView()) {
            resp.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        // 设置头信息
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Last-Modified", new Date(file.lastModified()).toString());
        resp.setHeader("Content-Type", mediaType.getMediaTypeWithCharset());
        resp.setHeader("Content-Length", String.valueOf(file.length()));
        resp.setHeader("Content-Disposition", "filename=" + URLEncoder.encode(uploadFileName, "UTF-8"));

        //获取输出流
        //通知客户端以下载的方式打开
        @Cleanup OutputStream out = resp.getOutputStream();
        if (mediaType.isOffice()) {
            resp.setHeader("Content-Type", "application/pdf;charset=utf-8");
            AsposeUtils.transToPdf(file.getAbsolutePath(), out);
        } else {
            FileUtils.writePos(file, out, 0, file.length());
        }
    }
}
