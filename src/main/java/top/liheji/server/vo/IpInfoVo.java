package top.liheji.server.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : Galaxy
 * @time : 2023/2/12 11:05
 * @create : IdeaJ
 * @project : server
 * @description :
 */
@NoArgsConstructor
@Data
public class IpInfoVo {
    private String ip;
    private String pro;
    private String proCode;
    private String city;
    private String cityCode;
    private String region;
    private String regionCode;
    private String addr;
    private String regionNames;
    private String err;
}
