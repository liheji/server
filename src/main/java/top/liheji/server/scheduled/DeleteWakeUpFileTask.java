package top.liheji.server.scheduled;

import java.io.File;

/**
 * @author : Galaxy
 * @time : 2023/2/15 17:44
 * @create : IdeaJ
 * @project : server
 * @description :
 */
public class DeleteWakeUpFileTask implements Runnable {
    private final File[] files;

    public DeleteWakeUpFileTask(File... files) {
        this.files = files;
    }

    @Override
    public void run() {
        for (File file : files) {
            if (!file.exists()) {
                continue;
            }
            // 尝试删除五次
            for (int i = 0; !file.delete() && i < 5; i++) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}