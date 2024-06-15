package top.yilee.server.util;

import org.springframework.util.ObjectUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : Galaxy
 * @time : 2022/1/17 14:00
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 字符串工具类
 */
public class StrUtils {
    public static final Pattern HTTP_RANGE_HEADER = Pattern.compile("bytes=(\\d*)-(\\d*)");

    /**
     * 解析HTTP Range头
     *
     * @param range range头
     * @param total 总长度
     * @return 返回解析结果
     */
    public static long[] parseRange(String range, long total) {
        long[] pos = new long[]{0, total - 1};

        // 有range参数
        if (ObjectUtils.isEmpty(range)) {
            return pos;
        }
        Matcher m = HTTP_RANGE_HEADER.matcher(range);
        if (m.find()) {
            String start = m.group(1).trim();
            String end = m.group(2).trim();
            if (start.isEmpty() && !end.isEmpty()) {
                pos[0] = pos[1] - Long.parseLong(end) + 1;
            } else {
                if (!start.isEmpty()) {
                    pos[0] = Long.parseLong(start);
                }
                if (!end.isEmpty()) {
                    pos[1] = Long.parseLong(end);
                }
                if (pos[0] > pos[1]) {
                    pos[0] = 0L;
                    pos[1] = total - 1;
                }
            }
        }

        return pos;
    }
}
