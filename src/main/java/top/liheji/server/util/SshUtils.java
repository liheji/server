package top.liheji.server.util;

import com.jcraft.jsch.*;
import lombok.Cleanup;
import top.liheji.server.pojo.other.FileItem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author : Galaxy
 * @time : 2022/1/17 14:00
 * @create : IdeaJ
 * @project : serverPlus
 * @description : SSH连接工具类
 */
public class SshUtils {
    private final JSch jSch;
    private final Session session;

    public SshUtils(String username, String host) throws Exception {
        this(username, host, 22);
    }

    public SshUtils(String username, String host, Integer port) throws Exception {
        this.jSch = new JSch();
        this.session = this.jSch.getSession(username, host, port);
        this.session.setConfig("StrictHostKeyChecking", "no");
        this.session.setUserInfo(new ServerUserInfo());
    }

    public void setPassword(String password) {
        this.session.setPassword(password);
    }

    public void setAuthFile(String authFile) throws Exception {
        this.jSch.addIdentity(authFile);
    }

    public void connect() throws Exception {
        this.session.setConfig("StrictHostKeyChecking", "no");
        this.session.connect(30000);
    }

    public String execute(String cmd) throws Exception {
        ChannelExec channel = (ChannelExec) this.session.openChannel("exec");
        channel.setPty(false);
        channel.setCommand(cmd);
        @Cleanup InputStream in = channel.getInputStream();
        channel.connect(30000);

        int i;
        byte[] bytes = new byte[1024];
        StringBuilder buffer = new StringBuilder();
        while (true) {
            while (in.available() > 0) {
                i = in.read(bytes);
                if (i < 0) {
                    break;
                }
                buffer.append(new String(bytes, 0, i));
            }

            if (channel.isClosed()) {
                if (in.available() > 0) {
                    continue;
                }
                channel.disconnect();
                break;
            }
        }

        return buffer.toString();
    }

    public boolean upload(String path, String filePath) throws Exception {
        File file = new File(filePath);
        return upload(path, file);
    }

    public boolean upload(String path, File file) throws Exception {
        @Cleanup InputStream in = new FileInputStream(file);
        return upload(path, in, file.getName());
    }

    public boolean upload(String path, ByteBuffer fileByte, String fileName) throws Exception {
        InputStream in = new ByteArrayInputStream(fileByte.array());
        return upload(path, in, fileName);
    }


    public void shell() throws Exception {
        Channel channel = this.session.openChannel("shell");
        channel.setInputStream(System.in);
        channel.setOutputStream(System.out);
        channel.connect(30000);
    }

    public void close() {
        if (this.session != null) {
            this.session.disconnect();
        }
    }

    public List<FileItem> genInfoList(String path) throws Exception {
        List<FileItem> fileList = new ArrayList<>();
        List<FileItem> dirList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S Z");
        String[] strs = execute(String.format("cd %s;ls -lA --time-style=full-iso", path)).split("\\s*\\n\\s*");
        for (String str : strs) {
            String[] files = str.split("\\s+");
            if (files.length < 9) {
                continue;
            }

            int dateI = 5, timeI = 6, zoneI = 7, nameI = 8;
            if (files[0].startsWith("b") || files[0].startsWith("c")) {
                dateI++;
                timeI++;
                zoneI++;
                nameI++;
            }

            files[timeI] = files[timeI].substring(0, 12);

            FileItem item = new FileItem(
                    files[0], files[2], files[3],
                    sdf.parse(String.join(" ", files[dateI], files[timeI], files[zoneI])).getTime(),
                    files[nameI]
            );

            if (files[0].startsWith("l")) {
                item.setLink(FileUtils.join(path, files[10]));
            }

            if (files[0].startsWith("-")) {
                item.setSize(Long.parseLong(files[4]));
                fileList.add(item);
            } else {
                dirList.add(item);
            }
        }

        // name
        fileList.sort(Comparator.comparing(FileItem::getName));
        dirList.sort(Comparator.comparing(FileItem::getName));

        dirList.addAll(fileList);

        return dirList;
    }

    private boolean upload(String path, InputStream in, String fileName) throws Exception {
        ChannelSftp sftp = (ChannelSftp) this.session.openChannel("sftp");
        try {
            // 进入目录
            sftp.cd(path);
        } catch (SftpException sException) {
            if (sException.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }
            // 指定上传路径不存在
            sftp.mkdir(path);
            sftp.cd(path);
        }

        sftp.put(in, fileName);
        sftp.disconnect();

        return true;
    }

    static class ServerUserInfo implements UserInfo {
        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public boolean promptPassword(String s) {
            return false;
        }

        @Override
        public boolean promptPassphrase(String s) {
            return false;
        }

        @Override
        public boolean promptYesNo(String s) {
            return true;
        }

        @Override
        public void showMessage(String s) {
        }
    }
}
