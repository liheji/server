package top.yilee.server.vo;

import lombok.Data;
import top.yilee.server.pojo.Account;
import top.yilee.server.util.SecurityUtils;

/**
 * @author : Galaxy
 * @time : 2023/2/12 10:32
 * @create : IdeaJ
 * @project : server
 * @description :
 */
@Data
public class RegisterVo {
    private String username;
    private String password;
    private String licence;

    public Account toAccount() {
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(SecurityUtils.bcrypt(password));
        account.setIsSuperuser(false);
        return account;
    }
}
