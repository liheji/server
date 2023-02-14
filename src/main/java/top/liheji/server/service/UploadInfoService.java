package top.liheji.server.service;

import top.liheji.server.pojo.FileInfo;
import top.liheji.server.pojo.UploadInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import top.liheji.server.util.page.PageUtils;
import top.liheji.server.vo.FileCheckVo;
import top.liheji.server.vo.UploadInfoVo;

import java.util.Map;

/**
 * @author Galaxy
 * @description 针对表【server_upload_info(上传信息)】的数据库操作Service
 * @createDate 2022-08-16 23:50:02
 */
public interface UploadInfoService extends IService<UploadInfo> {
    PageUtils queryPage(Map<String, Object> params, Long accountId);

    /**
     * 秒传
     *
     * @param checkVo   文件检查信息
     * @param accountId 用户ID
     * @return 上传信息
     */
    UploadInfoVo getUploadInfoVo(FileCheckVo checkVo, Long accountId);

    /**
     * 秒传
     *
     * @param param     检索信息
     * @param accountId 用户ID
     * @return 上传信息
     */
    UploadInfoVo getUploadInfoVo(String param, Long accountId);

    /**
     * 保存上传信息
     *
     * @param fileInfo   文件信息
     * @param uploadInfo 上传信息
     * @return 上传信息
     */
    UploadInfoVo saveUploadInfo(FileInfo fileInfo, UploadInfo uploadInfo);
}
