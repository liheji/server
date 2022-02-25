package top.liheji.server.config.remember;

import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import top.liheji.server.config.remember.impl.CustomTokenRememberMeServices;
import top.liheji.server.pojo.PersistentDevices;

import java.util.Date;

/**
 * The abstraction used by {@link CustomTokenRememberMeServices} to store the
 * persistent login tokens for a user.
 *
 * @author Luke Taylor
 * @since 2.0
 */
public interface CustomPersistentTokenRepository {

    /**
     * 创建新的Token
     *
     * @param token token
     */
    void createNewToken(PersistentRememberMeToken token);

    /**
     * 更新Token
     *
     * @param series     ID
     * @param tokenValue tokenValue
     * @param lastUsed   lastUsed
     */
    void updateToken(String series, String tokenValue, Date lastUsed);

    /**
     * 根据ID获取Token
     *
     * @param seriesId ID
     * @return Token
     */
    PersistentRememberMeToken getTokenForSeries(String seriesId);

    /**
     * 根据ID删除Token
     *
     * @param series ID
     */
    void removeUserToken(String series);

    /**
     * 删除不在Device中的Token
     *
     * @param username 用户名
     */
    void deleteTokenNotInDevice(String username);

    /**
     * 创建新的device
     *
     * @param device device
     */
    void createNewDevice(PersistentDevices device);

    /**
     * 更新Device
     *
     * @param device Device
     */
    void updateDevice(PersistentDevices device);

    /**
     * 更新Token ID
     *
     * @param series   Token ID
     * @param type     类型
     * @param username 用户名
     */
    void updateDeviceSeries(String series, String type, String username);

    /**
     * 更新Token ID
     *
     * @param series Token ID
     */
    void updateDeviceSeriesEmpty(String series);

    /**
     * 查询Device是否存在
     *
     * @param type     类型
     * @param username 用户名
     * @return 是否存在
     */
    boolean isExitDevice(String type, String username);
}