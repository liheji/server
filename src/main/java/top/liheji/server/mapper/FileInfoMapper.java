package top.liheji.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.liheji.server.pojo.FileInfo;

/**
 * @author Galaxy
 * @description 针对表【server_file_info(文件信息)】的数据库操作Mapper
 * @createDate 2022-08-16 23:50:02
 * @Entity top.liheji.server.pojo.FileInfo
 */
@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {

}