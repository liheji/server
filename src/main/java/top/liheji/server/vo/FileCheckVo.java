package top.liheji.server.vo;

import lombok.Data;

/**
 * @author : Galaxy
 * @time : 2023/1/24 22:46
 * @create : IdeaJ
 * @project : server
 * @description :
 */

@Data
public class FileCheckVo {
    private Long fileSize;
    private String fileHash;
    private String fileName;
}
