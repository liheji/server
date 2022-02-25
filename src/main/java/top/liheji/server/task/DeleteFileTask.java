package top.liheji.server.task;

import top.liheji.server.util.FileUtils;

import java.util.TimerTask;

/**
 * @author Galaxy
 */
public class DeleteFileTask extends TimerTask {
    private final String fileName;

    public DeleteFileTask(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void run() {
        FileUtils.resourceFile("files", fileName).deleteOnExit();
    }
}