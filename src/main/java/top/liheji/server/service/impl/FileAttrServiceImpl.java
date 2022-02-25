package top.liheji.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.liheji.server.mapper.FileAttrMapper;
import top.liheji.server.pojo.FileAttr;
import top.liheji.server.service.FileAttrService;

/**
* @author Galaxy
* @description 针对表【server_file_attr(文件信息)】的数据库操作Service实现
* @createDate 2022-01-25 15:03:20
*/
@Service("fileAttrService")
public class FileAttrServiceImpl extends ServiceImpl<FileAttrMapper, FileAttr>
    implements FileAttrService{

}




