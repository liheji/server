package top.yilee.server;

import top.yilee.server.util.HrbeuUtils;

import java.io.File;
import java.io.FileInputStream;


public class HrbeuFormat {
    public static void main(String[] args) throws Exception {
        File file = new File("new.html");
        FileInputStream in = new FileInputStream(file);
        File genFile = HrbeuUtils.dealWakeupSchedule(in, file.getName());
    }
}
