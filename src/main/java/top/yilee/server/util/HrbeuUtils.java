package top.yilee.server.util;

import lombok.Cleanup;
import lombok.NonNull;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.lang.Nullable;
import top.yilee.server.vo.CourseVo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : Galaxy
 * @time : 2021/10/29 22:29
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 哈尔滨工程大学课程表工具类
 */
public class HrbeuUtils {
    //需要用到的正则表达式
    private static final Pattern GRADE_REGEX = Pattern.compile("([\\d]+班)");
    private static final Pattern TEACH_REGEX = Pattern.compile("[(（](.*)[)）]");
    private static final Pattern NUM_NODE = Pattern.compile("\\d+");
    private static final Pattern NUM_RANGE_REGEX = Pattern.compile("(\\d+)(-(\\d+))?");

    /**
     * 将文件处理为Wakeup课程表可识别的 csv本地文件
     *
     * @param name 源文件流
     * @param name 源文件名
     * @return 新文件
     * @throws Exception 异常
     */
    public static @Nullable
    File dealWakeupSchedule(InputStream in, String name) throws Exception {
        if (name == null || !name.endsWith(".html")) {
            return null;
        }
        List<CourseVo> sourceCourses = genCourseListByHtml(in);

        String[] headers = new String[]{"课程名称", "星期", "开始节数", "结束节数", "老师", "地点", "周数"};
        // 创建CSV文件并写入数据
        File newFile = FileUtils.getUniqueFile(".csv", "uploads");
        try (FileWriter writer = new FileWriter(newFile);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.Builder.create().setHeader(headers).build())) {

            for (CourseVo course : sourceCourses) {
                Object[] writeData = new String[]{
                        course.getName(),
                        course.getDay().toString(),
                        course.getStartNode().toString(),
                        course.getEndNode().toString(),
                        course.getTeacher(),
                        course.getRoom(),
                        String.format("%d-%d", course.getStartWeek(), course.getEndWeek())
                };
                csvPrinter.printRecord(writeData);
            }
            return newFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 处理源文件为html的文件
     *
     * @param fis 源文件流
     * @return 处理好的课程列表
     * @throws Exception 异常
     */
    private static @NonNull List<CourseVo> genCourseListByHtml(InputStream fis) throws Exception {
        //格式化
        List<CourseVo> courseList = new ArrayList<>();

        //读取 html文件
        @Cleanup InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        @Cleanup BufferedReader br = new BufferedReader(isr);

        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = br.readLine()) != null) {
            builder.append(line);
        }

        Document doc = org.jsoup.Jsoup.parse(builder.toString());
        Element table1 = doc.getElementById("StuCul_TimetableQry_TimeTable").getElementsByClass("WtbodyZlistS").get(0);
        Elements trs = table1.getElementsByTag("tr");
        for (Element tr : trs) {
            Elements tds = tr.getElementsByTag("td");
            int countDay = 0;
            for (int i = 1; i < tds.size(); i++) {
                String courseSource = tds.get(i).html().replaceAll("(^<br>)|(<br>$)", "");
                if (courseSource.length() <= 5) {
                    countDay++;
                    continue;
                }

                for (String course : courseSource.split("<br>\\s*<br>")) {
                    String[] split = course.split("<br>");
                    if (split.length < 5) {
                        continue;
                    }

                    courseList.addAll(parseCourseInfo(Arrays.asList(split), countDay));
                }

                countDay++;
            }
        }

        return courseList;
    }

    /**
     * 解析每一节课
     *
     * @param split    拆分好的单个课程信息
     * @param countDay 当前日期（周一 ~ 周日）
     * @return 处理好的课程列表(如果一把多师或日期不连续则有多个)
     */
    private static @NonNull List<CourseVo> parseCourseInfo(List<String> split, int countDay) {
        List<CourseVo> resCourseList = new ArrayList<>();

        //去除类型词
        List<String> splitEnd = new ArrayList<>();
        for (String it : split) {
            splitEnd.add(it.substring(it.indexOf(':') + 1));
        }
        //获取信息
        //增加班级文字
        Matcher grade = GRADE_REGEX.matcher(splitEnd.get(1));
        if (grade.find()) {
            splitEnd.set(0, splitEnd.get(0) + "(" + grade.group(1) + ")");
        }

        Matcher numMatcher = NUM_NODE.matcher(splitEnd.get(2));
        numMatcher.find();
        String startStr = numMatcher.group();
        String endStr = startStr;
        while (numMatcher.find()) {
            endStr = numMatcher.group();
        }
        numMatcher.find();
        Integer startNode = Integer.parseInt(startStr);
        Integer endNode = Integer.parseInt(endStr);

        if (splitEnd.get(1).contains("一班多师")) {
            for (String item : splitEnd.get(3).split(";")) {
                Matcher itMatcher = NUM_RANGE_REGEX.matcher(item);
                while (itMatcher.find()) {
                    String startWeek = itMatcher.group(1).trim();
                    String endWeek = (itMatcher.group(3) == null || "".equals(itMatcher.group(3).trim())) ? itMatcher.group(1) : itMatcher.group(3);

                    Matcher teachMatcher = TEACH_REGEX.matcher(item);
                    teachMatcher.find();
                    String teacher = teachMatcher.group(1);
                    resCourseList.add(
                            new CourseVo(
                                    splitEnd.get(0).trim(),
                                    countDay,
                                    splitEnd.get(4).trim(),
                                    teacher.trim(),
                                    startNode,
                                    endNode,
                                    Integer.parseInt(startWeek),
                                    Integer.parseInt(endWeek),
                                    String.join(";", split.subList(5, split.size())).trim()
                            )
                    );
                }
            }
        } else {
            String teacher = splitEnd.get(1).substring(0, splitEnd.get(1).lastIndexOf(' '));
            Matcher itMatcher = NUM_RANGE_REGEX.matcher(splitEnd.get(3));
            while (itMatcher.find()) {
                String startWeek = itMatcher.group(1).trim();
                String endWeek = (itMatcher.group(3) == null || "".equals(itMatcher.group(3).trim())) ? itMatcher.group(1) : itMatcher.group(3);
                resCourseList.add(
                        new CourseVo(
                                splitEnd.get(0).trim(),
                                countDay,
                                splitEnd.get(4).trim(),
                                teacher.trim(),
                                startNode,
                                endNode,
                                Integer.parseInt(startWeek),
                                Integer.parseInt(endWeek),
                                String.join(";", split.subList(5, split.size())).trim()
                        )
                );
            }
        }
        return resCourseList;
    }
}