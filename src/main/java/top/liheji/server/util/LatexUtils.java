package top.liheji.server.util;

import okhttp3.*;
import com.alibaba.fastjson2.JSONObject;
import top.liheji.server.pojo.LatexAccount;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author : Galaxy
 * @time : 2022/10/17 11:37
 * @create : IdeaJ
 * @project : server
 * @description :
 */
public class LatexUtils {
    private static final String REG_BY_ACCOUNT_ACTION = "https://www.latexlive.com:5002/api/Client/RegByAccount";

    public static LatexAccount regByAccount(HttpServletRequest req) {
        String userAgent = req.getHeader("User-Agent");
        if (StringUtils.isEmpty(userAgent)) {
            return null;
        }
        okhttp3.MediaType mediaJson = okhttp3.MediaType.parse("application/json; charset=utf-8");
        LatexAccount latexAccount = new LatexAccount();
        latexAccount.setUsername(StringUtils.genRandString(15).toLowerCase());
        latexAccount.setPassword(StringUtils.genRandString(30));
        latexAccount.setEquiptype(userAgent);
        latexAccount.setTel("");
        latexAccount.setMail("");

        OkHttpClient client = new OkHttpClient();
        RequestBody postBody = RequestBody.create(mediaJson, latexAccount.registerForm());
        Request request = new Request.Builder()
                .post(postBody)
                .url(REG_BY_ACCOUNT_ACTION)
                .addHeader("User-Agent", userAgent)
                .build();

        try {
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            String respStr = response.body().string();

            JSONObject json = JSONObject.parseObject(respStr, JSONObject.class);

            if (json.getInteger("result").equals(0)) {
                return latexAccount;
            } else {
                return null;
            }
        } catch (IOException ignored) {
            return null;
        }
    }
}
