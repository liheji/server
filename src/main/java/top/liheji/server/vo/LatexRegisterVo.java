package top.liheji.server.vo;

import cn.hutool.core.util.RandomUtil;
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
public class LatexRegisterVo {
    private String username;
    private String password;
    private String equiptype;
    private String tel;
    private String mail;

    private String rowPwd;

    private LatexRegisterVo() {
    }

    public static LatexRegisterVo create(String userAgent) {
        LatexRegisterVo latexVo = new LatexRegisterVo();
        latexVo.setUsername(RandomUtil.randomString(15).toLowerCase());
        latexVo.setRowPwd(RandomUtil.randomString(30));
        latexVo.setEquiptype(userAgent);
        latexVo.setTel("");
        latexVo.setMail("");
        latexVo.setPassword(DigestUtil.md5Hex(latexVo.getRowPwd()));
        return latexVo;
    }

    public LatexAccount toPojo() {
        LatexAccount latexAccount = new LatexAccount();
        latexAccount.setUsername(this.username);
        latexAccount.setPassword(this.rowPwd);
        latexAccount.setEquiptype(this.equiptype);
        return latexAccount;
    }
}
