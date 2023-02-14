package top.liheji.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.liheji.server.pojo.AuthGroup;
import top.liheji.server.util.page.PageUtils;
import top.liheji.server.vo.AuthGroupVo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Galaxy
 * @description 针对表【auth_group(系统认证组)】的数据库操作Service
 * @createDate 2022-07-01 10:21:55
 */
public interface AuthGroupService extends IService<AuthGroup> {

    /**
     * 分页查询
     *
     * @param params 查询参数
     * @return 分页结果
     */
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存分组
     *
     * @param authGroupVo 分组信息
     */
    void saveGroup(AuthGroupVo authGroupVo);

    /**
     * 删除分组
     *
     * @param groupIds 分组 ID
     */
    void deleteBatchGroup(Collection<Long> groupIds);


    /**
     * 更新分组信息
     *
     * @param authGroupVo 分组信息
     */
    void updateGroup(AuthGroupVo authGroupVo);
}
