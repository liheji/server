package top.yilee.server.util;

import lombok.Cleanup;
import lombok.NonNull;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
    private static final String moreTeacher = "一班多师";
    private static final Pattern brTrimRegex = Pattern.compile("(^<br>)|(<br>$)");
    private static final Pattern numRangeRegex = Pattern.compile("(\\d+)(-(\\d+))?");
    private static final Pattern teachRegex = Pattern.compile("[(（](.*)[)）]");
    private static final Pattern gradeRegex = Pattern.compile("(\\d+班)");

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
        List<CourseVo> sourceCourseVos = genCourseVoListByHtml(in);

        String[] headers = new String[]{"课程名称", "星期", "开始节数", "结束节数", "老师", "地点", "周数"};
        // 创建CSV文件并写入数据
        File newFile = FileUtils.getUniqueFile(".csv", "uploads");
        try (FileWriter writer = new FileWriter(newFile);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.Builder.create().setHeader(headers).build())) {

            for (CourseVo course : sourceCourseVos) {
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
    private static @NonNull List<CourseVo> genCourseVoListByHtml(InputStream fis) throws Exception {
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
        List<Element> trs = table1.getElementsByTag("tr");
        List<CourseVo> courseList = new ArrayList<>();

        for (Element tr : trs) {
            List<Element> tds = tr.getElementsByTag("td");
            int dayIndex = 0;

            for (Element td : tds.subList(1, tds.size())) {
                String courseSource = brTrimRegex.matcher(td.html()).replaceAll("").trim();
                if (!courseSource.isEmpty()) {
                    String[] courses = courseSource.split("<br>\\s*<br>");
                    for (String course : courses) {
                        String[] courseInfo = course.split("<br>");
                        if (courseInfo.length >= 5) {
                            courseList.addAll(parseCourseVoInfo(courseInfo, dayIndex));
                        }
                    }
                }
                dayIndex++;
            }
        }
        return courseList;
    }

    /**
     * 解析每一节课
     *
     * @param courseInfo 课程信息
     * @param dayIndex   当前日期（周一 ~ 周日）
     * @return 处理好的课程列表(如果一班多师或节不连续则有多个)
     */
    private static List<CourseVo> parseCourseVoInfo(String[] courseInfo, int dayIndex) {
        List<String> infoList = new ArrayList<>();
        for (String info : courseInfo) {
            infoList.add(info.substring(info.indexOf(':') + 1).trim());
        }

        Matcher gradeMatcher = gradeRegex.matcher(infoList.get(1));
        String name = infoList.get(0) + (gradeMatcher.find() ? "(" + gradeMatcher.group(1) + ")" : "");
        String note = String.join(", ", String.join(", ", Arrays.copyOfRange(courseInfo, 5, courseInfo.length)));

        List<CourseVo> resList = new ArrayList<>();
        List<int[]> allNodeList = new ArrayList<>();

        Matcher matcher = numRangeRegex.matcher(infoList.get(2));
        while (matcher.find()) {
            int startNode = Integer.parseInt(matcher.group(1));
            int endNode = matcher.group(3) != null && !matcher.group(3).isEmpty() ? Integer.parseInt(matcher.group(3)) : startNode;
            allNodeList.add(new int[]{startNode, endNode});
        }

        for (int[] interval : mergeInterval(allNodeList)) {
            if (infoList.get(1).contains(moreTeacher)) {
                for (String item : infoList.get(3).split(";")) {
                    Matcher teachMatcher = teachRegex.matcher(item);
                    String teacher = teachMatcher.find() ? teachMatcher.group(1) : "";
                    resList.addAll(parseItem(item, name, dayIndex, infoList.get(4), teacher, interval[0], interval[1], note));
                }
            } else {
                String teacher = infoList.get(1).substring(0, infoList.get(1).lastIndexOf(" ")).trim();
                resList.addAll(parseItem(infoList.get(3), name, dayIndex, infoList.get(4), teacher, interval[0], interval[1], note));
            }
        }

        return resList;
    }

    private static List<CourseVo> parseItem(String item, String name, int day, String room, String teacher, int startNode, int endNode, String note) {
        List<int[]> allWeekList = new ArrayList<>();
        Matcher matcher = numRangeRegex.matcher(item);
        while (matcher.find()) {
            int startWeek = Integer.parseInt(matcher.group(1));
            int endWeek = matcher.group(3) != null && !matcher.group(3).isEmpty() ? Integer.parseInt(matcher.group(3)) : startWeek;
            allWeekList.add(new int[]{startWeek, endWeek});
        }

        List<CourseVo> courses = new ArrayList<>();
        for (int[] interval : mergeInterval(allWeekList)) {
            courses.add(new CourseVo(name, day, room, teacher, startNode, endNode, interval[0], interval[1], 0, 0f, note));
        }
        return courses;
    }

    // 合并区间 [1,1]和[2,2] -> [1,2]
    private static List<int[]> mergeInterval(List<int[]> intervals) {
        intervals.sort(Comparator.comparingInt(a -> a[0]));
        List<int[]> ans = new ArrayList<>();

        for (int[] interval : intervals) {
            if (ans.isEmpty() || ans.get(ans.size() - 1)[1] + 1 < interval[0]) {
                ans.add(interval);
            } else {
                int lastTo = ans.get(ans.size() - 1)[1];
                ans.get(ans.size() - 1)[1] = Math.max(lastTo, interval[1]);
            }
        }
        return ans;
    }
}