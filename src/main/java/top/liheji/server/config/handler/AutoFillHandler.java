package top.liheji.server.config.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author : Galaxy
 * @time : 2022/1/23 15:14
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 设置 MybatisPlus的自动填充属性
 */
@Component
public class AutoFillHandler implements MetaObjectHandler {

    //特殊字段
    /**
     * Account激活状态
     */
    private static final String FIELD_IS_ENABLED = "isEnabled";
    /**
     * Account是否管理员
     */
    private static final String FIELD_IS_SUPERUSER = "isSuperuser";

    /**
     * PersistentDevices使用时间
     */
    private static final String FIELD_LAST_USED = "lastUsed";

    //公共字段
    /**
     * 更新时间字段
     */
    private static final String FIELD_UPDATE_TIME = "updateTime";

    /**
     * 创建时间字段
     */
    private static final String FIELD_CREATE_TIME = "createTime";

    /**
     * Version
     */
    private static final String FIELD_VERSION = "version";

    /**
     * 插入元对象字段填充（用于插入时对公共字段的填充）
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        //特殊字段
        Date currentTime = new Date();
        if (metaObject.hasSetter(FIELD_IS_ENABLED)) {
            this.strictInsertFill(metaObject, FIELD_IS_ENABLED, Boolean.class, true);
        }

        if (metaObject.hasSetter(FIELD_IS_ENABLED)) {
            this.strictInsertFill(metaObject, FIELD_IS_SUPERUSER, Boolean.class, false);
        }

        if (metaObject.hasSetter(FIELD_LAST_USED)) {
            this.strictInsertFill(metaObject, FIELD_LAST_USED, Date.class, currentTime);
        }

        //公共字段
        if (metaObject.hasSetter(FIELD_CREATE_TIME)) {
            this.strictInsertFill(metaObject, FIELD_CREATE_TIME, Date.class, currentTime);
        }

        if (metaObject.hasSetter(FIELD_UPDATE_TIME)) {
            this.strictInsertFill(metaObject, FIELD_UPDATE_TIME, Date.class, currentTime);
        }

        if (metaObject.hasSetter(FIELD_VERSION)) {
            this.strictInsertFill(metaObject, FIELD_VERSION, Integer.class, 1);
        }
    }

    /**
     * 更新元对象字段填充（用于更新时对公共字段的填充）
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        //特殊字段
        Date currentTime = new Date();
        if (metaObject.hasSetter(FIELD_LAST_USED)) {
            this.strictInsertFill(metaObject, FIELD_LAST_USED, Date.class, currentTime);
        }

        //公共字段
        if (metaObject.hasSetter(FIELD_UPDATE_TIME)) {
            this.strictInsertFill(metaObject, FIELD_UPDATE_TIME, Date.class, currentTime);
        }
    }
}