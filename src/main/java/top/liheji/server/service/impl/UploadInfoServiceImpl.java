package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import top.liheji.server.service.FileInfoService;
import top.liheji.server.service.UploadInfoService;
import top.liheji.server.pojo.FileInfo;
import top.liheji.server.pojo.UploadInfo;
import top.liheji.server.mapper.UploadInfoMapper;
import org.springframework.stereotype.Service;
import top.liheji.server.util.page.PageUtils;
import top.liheji.server.util.page.Query;
import top.liheji.server.vo.FileCheckVo;
import top.liheji.server.vo.UploadInfoVo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Galaxy
 * @description 针对表【server_upload_info(上传信息)】的数据库操作Service实现
 * @createDate 2022-08-16 23:50:02
 */
@Service("uploadInfoService")
public class UploadInfoServiceImpl extends ServiceImpl<UploadInfoMapper, UploadInfo>
        implements UploadInfoService {

    @Autowired
    FileInfoService fileInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long accountId) {
        String fileName = (String) params.get("fileName");
        LambdaQueryWrapper<UploadInfo> queryWrapper = new LambdaQueryWrapper<UploadInfo>().like(UploadInfo::getAccountId, accountId);
        if (!ObjectUtils.isEmpty(fileName)) {
            queryWrapper = queryWrapper.like(UploadInfo::getFileName, fileName);
        }
        IPage<UploadInfo> page = this.page(
                new Query<UploadInfo>().getPage(params),
                queryWrapper
        );

        Set<Long> longSet = page.getRecords().stream().map(UploadInfo::getFileInfoId).collect(Collectors.toSet());
        Map<Long, FileInfo> fileInfoMap = fileInfoService.list(
                new LambdaQueryWrapper<FileInfo>()
                        .in(FileInfo::getId, longSet)
        ).stream().collect(Collectors.toMap(FileInfo::getId, it -> it));

        List<UploadInfoVo> uploadInfoVos = page.getRecords().stream().map(it -> {
            UploadInfoVo uploadInfoVo = new UploadInfoVo();
            BeanUtils.copyProperties(it, uploadInfoVo);
            uploadInfoVo.setFileInfo(fileInfoMap.get(it.getFileInfoId()));
            return uploadInfoVo;
        }).collect(Collectors.toList());

        // 重新设置分页数据
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(uploadInfoVos);
        return pageUtils;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UploadInfoVo getUploadInfoVo(FileCheckVo checkVo, Long accountId) {
        List<FileInfo> infoList = fileInfoService.list(
                new LambdaQueryWrapper<FileInfo>()
                        .eq(FileInfo::getFileSize, checkVo.getFileSize())
                        .eq(FileInfo::getFileHash, checkVo.getFileHash())
        );
        if (ObjectUtils.isEmpty(infoList)) {
            return null;
        }

        FileInfo fileInfo = infoList.get(0);
        UploadInfo uploadInfo = this.getOne(
                new LambdaQueryWrapper<UploadInfo>()
                        .eq(UploadInfo::getAccountId, accountId)
                        .eq(UploadInfo::getFileInfoId, fileInfo.getId())
        );

        if (ObjectUtils.isEmpty(uploadInfo)) {
            uploadInfo = new UploadInfo(checkVo.getFileName(), fileInfo.getId(), accountId);
            this.save(uploadInfo);
        }

        UploadInfoVo result = new UploadInfoVo();
        BeanUtils.copyProperties(uploadInfo, result);
        result.setFileInfo(fileInfo);
        return result;
    }

    @Override
    public UploadInfoVo getUploadInfoVo(String param, Long accountId) {
        LambdaQueryWrapper<UploadInfo> wrapper = new LambdaQueryWrapper<UploadInfo>()
                .eq(UploadInfo::getAccountId, accountId)
                .and(qw -> {
                    qw
                            .eq(UploadInfo::getId, param)
                            .or()
                            .eq(UploadInfo::getFileName, param);
                });

        List<UploadInfo> uploadInfoList = this.list(wrapper);
        if (ObjectUtils.isEmpty(uploadInfoList)) {
            return null;
        }

        UploadInfo uploadInfo = uploadInfoList.get(0);
        FileInfo fileInfo = fileInfoService.getById(uploadInfo.getFileInfoId());

        UploadInfoVo result = new UploadInfoVo();
        BeanUtils.copyProperties(uploadInfo, result);
        result.setFileInfo(fileInfo);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UploadInfoVo saveUploadInfo(FileInfo fileInfo, UploadInfo uploadInfo) {
        fileInfoService.save(fileInfo);
        uploadInfo.setFileInfoId(fileInfo.getId());
        this.save(uploadInfo);
        UploadInfoVo result = new UploadInfoVo();
        BeanUtils.copyProperties(uploadInfo, result);
        result.setFileInfo(fileInfo);
        return result;
    }
}




