package top.liheji.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.FileInfo;
import top.liheji.server.pojo.UploadInfo;
import top.liheji.server.service.CaptchaService;
import top.liheji.server.service.FileInfoService;
import top.liheji.server.service.UploadInfoService;
import top.liheji.server.util.AsposeUtils;
import top.liheji.server.util.FileUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author : Galaxy
 * @time : 2021/10/29 22:19
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 实现文件相关接口
 */
@RestController
@RequestMapping("/fileInfo")
public class FileInfoController {
    @Autowired
    CaptchaService captchaService;

    @Autowired
    FileInfoService fileInfoService;

    @Autowired
    UploadInfoService uploadInfoService;

    private static final Pattern R = Pattern.compile("bytes=(\\d*)-(\\d*)");

    @GetMapping
    @PreAuthorize("hasAuthority('view_file_info')")
    public Map<String, Object> queryFileInfo(Integer page, Integer limit,
                                             @RequestAttribute("account") Account current,
                                             @RequestParam(required = false, defaultValue = "") String fileName) {
        Page<UploadInfo> uploadInfoPage = uploadInfoService.page(
                new Page<>(page, limit),
                new LambdaQueryWrapper<UploadInfo>()
                        .like(UploadInfo::getFileName, fileName)
                        .eq(UploadInfo::getAccountId, current.getId())
        );
        List<UploadInfo> dataList = uploadInfoPage.getRecords();
        Map<String, Object> map = new HashMap<>(5);
        map.put("code", 0);
        map.put("msg", "查询成功");
        map.put("count", dataList.size());
        map.put("total", uploadInfoPage.getTotal());
        map.put("data", dataList);
        return map;
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('delete_file_info')")
    public Map<String, Object> deleteFileInfo(@RequestParam List<Integer> fileIds, @RequestAttribute("account") Account current) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 0);
        map.put("msg", "删除完成");
        map.put("count", uploadInfoService.getBaseMapper().delete(
                new LambdaQueryWrapper<UploadInfo>()
                        .eq(UploadInfo::getAccountId, current.getId())
                        .in(UploadInfo::getId, fileIds)
        ));
        map.put("total", fileIds.size());
        return map;
    }

    @PostMapping("verify")
    @PreAuthorize("hasAuthority('add_file_info')")
    public Map<String, Object> checkFileInfo(Long fileSize,
                                             String fileHash,
                                             String fileName,
                                             @RequestAttribute("account") Account current) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 1);
        map.put("msg", "OK");

        FileInfo fileInfo = fileInfoService.getOne(
                new LambdaQueryWrapper<FileInfo>()
                        .eq(FileInfo::getFileSize, fileSize)
                        .eq(FileInfo::getFileHash, fileHash)
        );
        if (fileInfo == null) {
            map.put("key", captchaService.genSecret(current.getUsername(), 5 * 60));
        } else {
            UploadInfo uploadInfo = uploadInfoService.getOne(
                    new LambdaQueryWrapper<UploadInfo>()
                            .eq(UploadInfo::getAccountId, current.getId())
                            .eq(UploadInfo::getFileInfoId, fileInfo.getId())
                            .eq(UploadInfo::getFileInfoId, fileInfo.getId())
            );

            if (uploadInfo == null) {
                uploadInfo = new UploadInfo(fileName, fileInfo.getId(), current.getId());
                uploadInfoService.save(uploadInfo);
            }

            uploadInfo.setFileInfo(fileInfo);

            map.put("code", 0);
            map.put("msg", "上传完成");
            map.put("data", uploadInfo);
        }

        return map;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('add_file_info')")
    public Map<String, Object> addFileInfo(@RequestParam("file") MultipartFile file,
                                           @RequestHeader(value = "UPLOAD-TOKEN", defaultValue = "") String uToken,
                                           @RequestAttribute("account") Account current) throws Exception {
        Map<String, Object> map = new HashMap<>(4);

        if (!captchaService.checkSecret(current.getUsername(), uToken)) {
            map.put("code", 1);
            map.put("msg", "上传被禁止");

            return map;
        }

        //上传文件
        FileInfo fileInfo = FileUtils.uploadFile(file);
        fileInfoService.save(fileInfo);
        UploadInfo uploadInfo = new UploadInfo(file.getOriginalFilename(), fileInfo.getId(), current.getId());
        uploadInfoService.save(uploadInfo);
        uploadInfo.setFileInfo(fileInfo);

        map.put("code", 0);
        map.put("msg", "上传成功");
        map.put("data", uploadInfo);

        return map;
    }

    @GetMapping("download/{param:.+}")
    @PreAuthorize("hasAuthority('download_file_info')")
    public void downloadFile(@PathVariable String param,
                             @RequestHeader(value = "Range", defaultValue = "") String range,
                             @RequestAttribute("account") Account current,
                             HttpServletResponse resp) throws IOException {

        LambdaQueryWrapper<UploadInfo> wrapper = new LambdaQueryWrapper<UploadInfo>()
                .eq(UploadInfo::getAccountId, current.getId());

        if (param.matches("^\\d+$")) {
            wrapper = wrapper.eq(UploadInfo::getId, Integer.parseInt(param));
        } else {
            wrapper = wrapper.eq(UploadInfo::getFileName, param);
        }

        List<UploadInfo> uploadInfoList = uploadInfoService.list(wrapper);
        if (uploadInfoList.size() <= 0) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        UploadInfo uploadInfo = uploadInfoList.get(0);

        //get方式提交的
        File file = FileUtils.staticFile("uploads", uploadInfo.getFileInfo().getFileName());
        if (!file.exists()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long fSize = file.length();
        long startPos = 0L;
        long endPos = fSize - 1;
        Matcher m = R.matcher(range);
        if (m.find()) {
            String start = m.group(1).trim();
            String end = m.group(2).trim();
            if ("".equals(start) && !"".equals(end)) {
                startPos = endPos - Long.parseLong(end) + 1;
            } else {
                startPos = Long.parseLong(start.length() > 0 ? start : "0");
                endPos = Long.parseLong(end.length() > 0 ? end : String.valueOf(endPos));

                if (startPos > endPos) {
                    startPos = 0L;
                    endPos = fSize - 1;
                }
            }
        }

        long contentLength = endPos - startPos + 1;

        //通知客户端以下载的方式打开
        resp.setContentLength((int) file.length());
        resp.setHeader("Accept-Ranges", "bytes");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(uploadInfo.getFileName(), "UTF-8"));
        resp.setHeader("Content-Length", String.valueOf(contentLength));
        resp.setHeader("Content-Range", String.format("bytes %d-%d/%d", startPos, endPos, fSize));
        resp.setHeader("Content-Type", "application/octet-stream");
        resp.setHeader("Last-Modified", new Date(file.lastModified()).toString());

        if (startPos == 0 && endPos == fSize - 1) {
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        }

        //获取输出流
        @Cleanup OutputStream out = resp.getOutputStream();

        //构建任意读取输入流
        @Cleanup RandomAccessFile raf = new RandomAccessFile(file, "r");

        FileUtils.writePos(out, raf, startPos, contentLength);
    }

    @GetMapping("preview/{param:.+}")
    @PreAuthorize("hasAuthority('download_file_info')")
    public void previewFile(@PathVariable String param,
                            @RequestAttribute("account") Account current,
                            HttpServletResponse resp) throws Exception {
        param = param == null ? "" : param;

        LambdaQueryWrapper<UploadInfo> wrapper = new LambdaQueryWrapper<UploadInfo>()
                .eq(UploadInfo::getAccountId, current.getId());

        if (param.matches("^\\d+$")) {
            wrapper = wrapper.eq(UploadInfo::getId, Integer.parseInt(param));
        } else {
            wrapper = wrapper.eq(UploadInfo::getFileName, param);
        }

        List<UploadInfo> uploadInfoList = uploadInfoService.list(wrapper);
        if (uploadInfoList.size() <= 0) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        UploadInfo uploadInfo = uploadInfoList.get(0);

        //get方式提交的
        File file = FileUtils.staticFile("uploads", uploadInfo.getFileInfo().getFileName());
        if (!file.exists()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = FileUtils.guessMediaType(file);
        if (Pattern.matches("^(text|video|audio|image)/.*", contentType) ||
                Pattern.matches("application/.*(script|json|pdf|xml)", contentType) ||
                Pattern.matches(".*\\.(doc|xls|ppt)x?$", file.getName())) {
            contentType += ";charset=utf-8";
        } else {
            contentType = null;
        }

        if (contentType == null) {
            resp.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        } else {
            resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            resp.setHeader("Last-Modified", new Date(file.lastModified()).toString());
        }

        //获取输出流
        //通知客户端以下载的方式打开
        FileInputStream in = new FileInputStream(file);
        OutputStream out = resp.getOutputStream();
        if (Pattern.matches(".*\\.(doc|xls|ppt)x?$", file.getName())) {
            resp.setHeader("Content-Type", "application/pdf");
            resp.setHeader("Content-Disposition",
                    "filename=" + URLEncoder.encode(uploadInfo.getFileName() + ".pdf", "UTF-8"));
            AsposeUtils.transToPdf(file.getAbsolutePath(), out);
            in.close();
        } else {
            resp.setHeader("Content-Type", contentType);
            resp.setHeader("Content-Length", String.valueOf(file.length()));
            resp.setHeader("Content-Disposition",
                    "filename=" + URLEncoder.encode(uploadInfo.getFileName(), "UTF-8"));

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = in.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }
            in.close();
            out.close();
        }
    }
}
