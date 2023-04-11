package top.liheji.server.vo;

import cn.hutool.crypto.digest.DigestUtil;
import lombok.Data;
import top.liheji.server.pojo.LatexAccount;

/**
 * @author : Galaxy
 * @time : 2023/2/12 10:32
 * @create : IdeaJ
 * @project : server
 * @description :
 */
@Data
public class LatexLoginVo {
    private String username;
    private String password;

    private LatexLoginVo() {
    }

    public static LatexLoginVo fromPojo(LatexAccount account) {
        LatexLoginVo latexVo = new LatexLoginVo();
        latexVo.setUsername(account.getUsername());
        latexVo.setPassword(DigestUtil.md5Hex(account.getPassword()));
        return latexVo;
    }
}
