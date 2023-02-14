package top.liheji.server.vo;

import lombok.Data;
import top.liheji.server.constant.MessageDigestEnum;
import top.liheji.server.pojo.LatexAccount;
import top.liheji.server.util.CypherUtils;
import top.liheji.server.util.StringUtils;

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
        latexVo.setUsername(StringUtils.getRandString(15).toLowerCase());
        latexVo.setRowPwd(StringUtils.getRandString(30));
        latexVo.setEquiptype(userAgent);
        latexVo.setTel("");
        latexVo.setMail("");
        latexVo.setPassword(CypherUtils.encodeToHash(latexVo.getRowPwd(), MessageDigestEnum.MD5).toLowerCase());
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
