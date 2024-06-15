package top.yilee.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.yilee.server.pojo.UploadInfo;

/**
* @author Galaxy
* @description 针对表【server_upload_info(上传信息)】的数据库操作Mapper
* @createDate 2022-08-16 23:50:02
* @Entity top.yilee.server.pojo.UploadInfo
*/
@Mapper
public interface UploadInfoMapper extends BaseMapper<UploadInfo> {

}
