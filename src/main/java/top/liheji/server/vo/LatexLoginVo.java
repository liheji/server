package top.liheji.server.vo;

import lombok.Data;
import top.liheji.server.constant.MessageDigestEnum;
import top.liheji.server.pojo.LatexAccount;
import top.liheji.server.util.CypherUtils;

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
        latexVo.setPassword(CypherUtils.encodeToHash(account.getPassword(), MessageDigestEnum.MD5).toLowerCase());
        return latexVo;
    }
}
