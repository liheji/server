package top.liheji.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.FileAttr;
import top.liheji.server.service.FileAttrService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import top.liheji.server.util.AsposeUtil;
import top.liheji.server.util.FileUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Galaxy
 */
@RestController
@RequestMapping("/fileAttr")
public class FileAttrController {

    @Autowired
    FileAttrService fileAttrService;

    private static final Pattern R = Pattern.compile("bytes=(\\d*)-(\\d*)");

    @GetMapping
    public Map<String, Object> queryFileAttr(Integer page, Integer limit,
                                             @RequestAttribute("account") Account current,
                                             @RequestParam(required = false, defaultValue = "") String fileName) {
        Page<FileAttr> fileAttrPage = fileAttrService.page(
                new Page<>(page, limit),
                new QueryWrapper<FileAttr>()
                        .like("file_name", fileName)
                        .eq("account_id", current.getId())
        );
        List<FileAttr> dataList = fileAttrPage.getRecords();
        Map<String, Object> map = new HashMap<>(5);
        map.put("code", 0);
        map.put("msg", "查询成功");
        map.put("count", dataList.size());
        map.put("total", fileAttrPage.getTotal());
        map.put("data", dataList);
        return map;
    }

    @DeleteMapping
    public Map<String, Object> deleteFileAttr(String id, @RequestAttribute("account") Account current) {
        String[] ids = id.split(",");
        int del = 0;
        for (String sp : ids) {
            Integer idd = Integer.parseInt(sp);
            FileAttr attr = fileAttrService.getById(idd);
            File file = FileUtils.resourceFile("files", attr.getFileName());
            if (
                    fileAttrService.remove(new QueryWrapper<FileAttr>()
                            .eq("id", idd)
                            .eq("account_id", current.getId()))
            ) {
                file.deleteOnExit();
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

    @PostMapping
    public Map<String, Object> insertFileAttr(@RequestParam("file") MultipartFile[] file,
                                              @RequestAttribute("account") Account current) throws Exception {
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 0);
        map.put("msg", "上传成功");

        //上传文件
        FileAttr[] fileAttrs = FileUtils.uploadFiles(file);

        List<FileAttr> fileInfoList = new ArrayList<>();

        int len = 0;
        for (FileAttr fileAttr : fileAttrs) {
            fileAttr.setAccountId(current.getId());
            List<FileAttr> infoNew = fileAttrService.list(
                    new QueryWrapper<FileAttr>()
                            .eq("file_size", fileAttr.getFileSize())
                            .eq("file_hash", fileAttr.getFileHash())
            );
            if (infoNew.size() > 0) {
                boolean flag = false;
                for (FileAttr cur : infoNew) {
                    if (!current.getId().equals(cur.getAccountId())) {
                        fileAttrService.save(fileAttr);
                    } else {
                        map.put("msg", "某些文件已存在");
                        fileInfoList.add(cur);
                    }
                    flag |= FileUtils.resourceFile(cur.getFileName()).exists();
                }

                if (flag) {
                    FileUtils.resourceFile(fileAttr.getFileName()).delete();
                } else {
                    FileUtils.resourceFile(fileAttr.getFileName()).renameTo(
                            FileUtils.resourceFile(infoNew.get(0).getFileName())
                    );
                }
            } else {
                len++;
                fileAttrService.save(fileAttr);
                fileInfoList.add(fileAttr);
            }
        }
        map.put("data", fileInfoList);

        map.put("count", len);
        map.put("total", file.length);

        return map;
    }

    @GetMapping("download")
    public void downloadFile(@RequestParam(defaultValue = "") String param,
                             @RequestHeader(value = "Range", defaultValue = "") String rang,
                             @RequestAttribute("account") Account current,
                             HttpServletResponse resp) throws IOException {

        if (param.matches("^\\d+$")) {
            Wrapper<FileAttr> wrapper = new QueryWrapper<FileAttr>()
                    .eq("id", Integer.parseInt(param))
                    .eq("account_id", current.getId());

            List<FileAttr> fileAttrList = fileAttrService.list(wrapper);
            if (fileAttrList.size() <= 0) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            param = fileAttrList.get(0).getFileName();
        }

        //get方式提交的
        File file = FileUtils.resourceFile("files", param);
        if (!file.exists()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long fSize = file.length();
        long startPos = 0L;
        long endPos = fSize - 1;
        Matcher m = R.matcher(rang);
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
        resp.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(param, "UTF-8"));
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
    public void previewFile(@PathVariable String param,
                            @RequestAttribute("account") Account current,
                            HttpServletResponse resp) throws Exception {
        param = param == null ? "" : param;

        if (param.matches("^\\d+$")) {
            Wrapper<FileAttr> wrapper = new QueryWrapper<FileAttr>()
                    .eq("id", Integer.parseInt(param))
                    .eq("account_id", current.getId());

            List<FileAttr> fileAttrList = fileAttrService.list(wrapper);
            if (fileAttrList.size() <= 0) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            param = fileAttrList.get(0).getFileName();
        }

        //get方式提交的
        File file = FileUtils.resourceFile("files", param);
        if (!file.exists()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }


        String contentType = FileUtils.fileMimeType(file);
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
            resp.setHeader("Content-Disposition", "filename=" + URLEncoder.encode(param + ".pdf", "UTF-8"));
            AsposeUtil.transToPdf(file.getAbsolutePath(), out);
            in.close();
        } else {
            resp.setHeader("Content-Type", contentType);
            resp.setHeader("Content-Length", String.valueOf(file.length()));
            resp.setHeader("Content-Disposition", "filename=" + URLEncoder.encode(param, "UTF-8"));

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
