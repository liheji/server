package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import top.liheji.server.pojo.UploadInfo;
import top.liheji.server.service.UploadInfoService;
import top.liheji.server.mapper.UploadInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author Galaxy
* @description 针对表【server_upload_info(上传信息)】的数据库操作Service实现
* @createDate 2022-08-16 23:50:02
*/
@Service("uploadInfoService")
public class UploadInfoServiceImpl extends ServiceImpl<UploadInfoMapper, UploadInfo>
    implements UploadInfoService{

}




