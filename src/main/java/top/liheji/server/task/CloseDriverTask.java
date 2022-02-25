package top.liheji.server.task;

import lombok.SneakyThrows;
import top.liheji.server.util.DriverUtils;

import java.util.TimerTask;

/**
 * @author Galaxy
 */
public class CloseDriverTask extends TimerTask {
    private static int TOTAL = 0;

    public CloseDriverTask() {
        TOTAL++;
    }

    @SneakyThrows
    @Override
    public void run() {
        if (TOTAL == 1) {
            DriverUtils.getInstance().close();
        }
        TOTAL--;
    }
}