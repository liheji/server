package top.liheji.server.vo;

import lombok.Data;
import top.liheji.server.util.SecurityUtils;

/**
 * @author : Galaxy
 * @time : 2023/2/13 23:52
 * @create : IdeaJ
 * @project : server
 * @description :
 */
@Data
public class ForgetVo {
    private String username;
    private String password;
    private String key;

    public String bcrypt() {
        return SecurityUtils.bcrypt(password);
    }
}
