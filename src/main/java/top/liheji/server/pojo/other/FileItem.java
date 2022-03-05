package top.liheji.server.pojo.other;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : Galaxy
 * @time : 2022/1/17 21:28
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 服务器文件信息实体
 */
@Data
public class FileItem implements Serializable {
    private static final long serialVersionUID = -4780179592202698586L;

    private String permit;
    private String user;
    private String group;
    private Long size;
    private Long time;
    private String name;
    private String link;
    private Boolean isFile;

    public FileItem() {
        this.size = 0L;
        this.link = "";
    }

    public FileItem(String permit, String user, String group, Long time, String name) {
        this();
        this.permit = permit;
        this.isFile = permit.startsWith("-");
        this.user = user;
        this.group = group;
        this.time = time;
        this.name = name;
    }
}
