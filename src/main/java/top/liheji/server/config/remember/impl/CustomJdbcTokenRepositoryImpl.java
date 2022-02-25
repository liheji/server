package top.liheji.server.config.remember.impl;


import org.springframework.core.log.LogMessage;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import top.liheji.server.config.remember.CustomPersistentTokenRepository;
import top.liheji.server.pojo.PersistentDevices;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * JDBC based persistent login token repository implementation.
 * 重写 {@link JdbcTokenRepositoryImpl} 类
 * 增加：登录设备数据库操作
 * 修改：登录token的单个删除
 *
 * @author Luke Taylor
 * @since 2.0
 */
public class CustomJdbcTokenRepositoryImpl extends JdbcDaoSupport implements CustomPersistentTokenRepository {

    /**
     * Default SQL for creating the database table to store the tokens
     */
    public static final String CREATE_TABLE_SQL = "create table persistent_logins (username varchar(64) not null, series varchar(64) primary key, "
            + "token varchar(64) not null, last_used timestamp not null)";

    /**
     * The default SQL used by the <tt>getTokenBySeries</tt> query
     */
    public static final String DEF_TOKEN_BY_SERIES_SQL = "select username,series,token,last_used from persistent_logins where series = ?";

    /**
     * The default SQL used by <tt>createNewToken</tt>
     */
    public static final String DEF_INSERT_TOKEN_SQL = "insert into persistent_logins (username, series, token, last_used) values(?,?,?,?)";

    /**
     * The default SQL used by <tt>updateToken</tt>
     */
    public static final String DEF_UPDATE_TOKEN_SQL = "update persistent_logins set token = ?, last_used = ? where series = ?";

    /**
     * ************* 修改此处 ***************
     * 表persistent_logins
     */
    public static final String CUSTOM_REMOVE_USER_TOKENS_SQL = "delete from persistent_logins where series = ?";

    public static final String CUSTOM_DELETE_NOT_IN_DEVICE_SQL = "delete from persistent_logins where username = ? and series not in (select series from persistent_devices where username = ?)";

    /**
     * ************* 修改此处 ***************
     * 表persistent_devices
     */
    public static final String CUSTOM_CREATE_TABLE_SQL = "create table persistent_devices (type varchar(16) not null, username varchar(64) not null, "
            + "series varchar(64), browser varchar(255) not null, operate_system varchar(255) not null, last_used datetime not null, primary key (type, username))";

    public static final String CUSTOM_SELECT_DEVICE_SQL = "select type,username,series,browser,operate_system,last_used from persistent_devices where type = ? and username = ?";

    public static final String CUSTOM_INSERT_DEVICE_SQL = "insert into persistent_devices (type,username,series,browser,operate_system,last_used) values(?,?,?,?,?,?)";

    public static final String CUSTOM_UPDATE_DEVICE_SQL = "update persistent_devices set series = ?, browser = ?, operate_system = ?, last_used = ? where type = ? and username = ?";

    public static final String CUSTOM_EXPIRE_DEVICE_SQL = "update persistent_devices set series = ? where type = ? and username = ?";

    public static final String CUSTOM_EMPTY_DEVICE_SQL = "update persistent_devices set series = '' where series = ?";

    private String tokensBySeriesSql = DEF_TOKEN_BY_SERIES_SQL;

    private String insertTokenSql = DEF_INSERT_TOKEN_SQL;

    private String updateTokenSql = DEF_UPDATE_TOKEN_SQL;

    private String removeUserTokensSql = CUSTOM_REMOVE_USER_TOKENS_SQL;

    private boolean createTableOnStartup;

    @Override
    protected void initDao() {
        if (this.createTableOnStartup) {
            getJdbcTemplate().execute(CREATE_TABLE_SQL);
            // ************* 修改此处 ***************
            getJdbcTemplate().execute(CUSTOM_CREATE_TABLE_SQL);
        }
    }

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        getJdbcTemplate().update(this.insertTokenSql, token.getUsername(), token.getSeries(), token.getTokenValue(),
                token.getDate());
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        getJdbcTemplate().update(this.updateTokenSql, tokenValue, lastUsed, series);
    }

    /**
     * Loads the token data for the supplied series identifier.
     * <p>
     * If an error occurs, it will be reported and null will be returned (since the result
     * should just be a failed persistent login).
     *
     * @param seriesId
     * @return the token matching the series, or null if no match found or an exception
     * occurred.
     */
    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        try {
            return getJdbcTemplate().queryForObject(this.tokensBySeriesSql, this::createRememberMeToken, seriesId);
        } catch (EmptyResultDataAccessException ex) {
            this.logger.debug(LogMessage.format("Querying token for series '%s' returned no results.", seriesId), ex);
        } catch (IncorrectResultSizeDataAccessException ex) {
            this.logger.error(LogMessage.format(
                    "Querying token for series '%s' returned more than one value. Series" + " should be unique",
                    seriesId));
        } catch (DataAccessException ex) {
            this.logger.error("Failed to load token for series " + seriesId, ex);
        }
        return null;
    }

    private PersistentRememberMeToken createRememberMeToken(ResultSet rs, int rowNum) throws SQLException {
        return new PersistentRememberMeToken(rs.getString(1), rs.getString(2), rs.getString(3), rs.getTimestamp(4));
    }


    /**
     * Intended for convenience in debugging. Will create the persistent_tokens database
     * table when the class is initialized during the initDao method.
     *
     * @param createTableOnStartup set to true to execute the
     */
    public void setCreateTableOnStartup(boolean createTableOnStartup) {
        this.createTableOnStartup = createTableOnStartup;
    }

    //************* 修改此处 ***************

    @Override
    public void removeUserToken(String series) {
        getJdbcTemplate().update(this.removeUserTokensSql, series);
    }

    @Override
    public void deleteTokenNotInDevice(String username) {
        getJdbcTemplate().update(CUSTOM_DELETE_NOT_IN_DEVICE_SQL, username, username);
    }

    @Override
    public void createNewDevice(PersistentDevices device) {
        getJdbcTemplate().update(CUSTOM_INSERT_DEVICE_SQL, device.getType(), device.getUsername(), device.getSeries(),
                device.getBrowser(), device.getOperateSystem(), device.getLastUsed());
    }

    @Override
    public boolean isExitDevice(String type, String username) {
        List<PersistentDevices> deviceList = getJdbcTemplate().query(CUSTOM_SELECT_DEVICE_SQL,
                new BeanPropertyRowMapper<>(PersistentDevices.class), type, username);
        return deviceList.size() == 1;
    }

    @Override
    public void updateDevice(PersistentDevices device) {
        getJdbcTemplate().update(CUSTOM_UPDATE_DEVICE_SQL, device.getSeries(), device.getBrowser(),
                device.getOperateSystem(), device.getLastUsed(), device.getType(), device.getUsername());
    }

    @Override
    public void updateDeviceSeries(String series, String type, String username) {
        getJdbcTemplate().update(CUSTOM_EXPIRE_DEVICE_SQL, series, type, username);
    }

    @Override
    public void updateDeviceSeriesEmpty(String series) {
        getJdbcTemplate().update(CUSTOM_EMPTY_DEVICE_SQL, series);
    }
}
