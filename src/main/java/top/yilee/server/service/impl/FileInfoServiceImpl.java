package top.yilee.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import top.yilee.server.service.FileInfoService;
import top.yilee.server.pojo.FileInfo;
import top.yilee.server.mapper.FileInfoMapper;
import org.springframework.stereotype.Service;

/**
 * @author Galaxy
 * @description 针对表【server_file_info(文件信息)】的数据库操作Service实现
 * @createDate 2022-08-16 23:50:02
 */
@Service("fileInfoService")
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo>
        implements FileInfoService {
}




