package top.liheji.server.util;

import com.csvreader.CsvWriter;
import lombok.NonNull;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.lang.Nullable;
import top.liheji.server.pojo.other.Course;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Galaxy
 */
public class HrbeuUtils {
    //需要用到的正则表达式
    private static final Pattern GRADE_REGEX = Pattern.compile("([\\d]+班)");
    private static final Pattern TEACH_REGEX = Pattern.compile("[(（](.*)[)）]");
    private static final Pattern NUM_RANGE_REGEX = Pattern.compile("(\\d+)(-(\\d+))?");
    private static final String[] NUM = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};

    /**
     * 将文件处理为Wakeup课程表可识别的本地文件
     *
     * @param file 源文件
     * @return 新文件
     * @throws IOException 异常
     */
    public static @Nullable
    File dealWakeupSchedule(File file) throws IOException {
        String name = file.getName();
        File newFile = FileUtils.resourceFile("files", CypherUtils.genUuid() + ".csv");

        if (!newFile.getParentFile().exists()) {
            newFile.getParentFile().mkdirs();
        }

        CsvWriter csvWriter = new CsvWriter(new FileOutputStream(newFile), ',', StandardCharsets.UTF_8);

        List<Course> sourceCourses;
        if (name.endsWith(".html")) {
            sourceCourses = genCourseListByHtml(file);
        } else if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
            sourceCourses = genCourseListByXls(file);
        } else {
            return null;
        }

        for (Course course : sourceCourses) {
            csvWriter.writeRecord(new String[]{
                    course.getName(),
                    course.getDay().toString(),
                    course.getStartNode().toString(),
                    course.getEndNode().toString(),
                    course.getTeacher(),
                    course.getRoom(),
                    String.format("%d-%d", course.getStartWeek(), course.getEndWeek())
            });
        }
        csvWriter.close();

        return newFile;
    }


    /**
     * 将文件处理为TimeTable课程表可识别的本地文件
     *
     * @param file 源文件
     * @return 新文件
     * @throws IOException 异常
     */
    public static @Nullable
    File dealTimeTable(File file) throws IOException {
        String name = file.getName();
        File newFile = FileUtils.resourceFile("files", CypherUtils.genUuid() + ".xls");

        if (!newFile.getParentFile().exists()) {
            newFile.getParentFile().mkdirs();
        }

        Workbook simpleWb = new HSSFWorkbook();
        Sheet simpleSheet = simpleWb.createSheet("Sheet0");

        List<Course> sourceCourses;
        if (name.endsWith(".html")) {
            sourceCourses = genCourseListByHtml(file);
        } else if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
            sourceCourses = genCourseListByXls(file);
        } else {
            return null;
        }
        int i = 0;
        for (Course course : sourceCourses) {
            Row newRow = simpleSheet.createRow(i);
            String[] writeData = new String[]{
                    course.getName(),
                    course.getTeacher(),
                    NUM[course.getDay()],
                    String.format("%d-%d", course.getStartNode(), course.getEndNode()),
                    course.getRoom(),
                    String.format("%d-%d", course.getStartWeek(), course.getEndWeek())
            };

            for (int j = 0; j < writeData.length; j++) {
                newRow.createCell(j).setCellValue(writeData[j]);
            }

            i++;
        }

        FileOutputStream out = new FileOutputStream(newFile);
        simpleWb.write(out);

        out.close();
        simpleWb.close();

        return newFile;
    }


    /**
     * 处理源文件为xls和xlsx的文件
     *
     * @param file 源文件
     * @return 处理好的课程列表
     * @throws IOException 异常
     */
    private static @NonNull List<Course> genCourseListByXls(File file) throws IOException {
        List<Course> courseList = new ArrayList<>();

        FileInputStream fis = new FileInputStream(file);
        Workbook wb;
        //根据文件后缀（xls/xlsx）进行判断
        if (file.getName().toLowerCase().endsWith(".xls")) {
            wb = new HSSFWorkbook(fis);
        } else {
            wb = new XSSFWorkbook(fis);
        }

        Sheet sheet = wb.getSheetAt(0);
        //遍历行
        for (int i = sheet.getFirstRowNum() + 2; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                //遍历列
                int countDay = 1;
                for (int j = row.getFirstCellNum() + 1; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        String courseSource = cell.toString().trim();
                        if (courseSource.length() <= 5) {
                            countDay++;
                            continue;
                        }

                        for (String course : courseSource.split("\n\\s*\n")) {
                            String[] split = course.split("\n");
                            if (split.length < 5) {
                                continue;
                            }

                            courseList.addAll(parseCourseInfo(Arrays.asList(split), countDay));
                        }
                    }
                    countDay++;
                }
            }
        }

        return courseList;
    }

    /**
     * 处理源文件为html的文件
     *
     * @param file 源文件
     * @return 处理好的课程列表
     * @throws IOException 异常
     */
    private static @NonNull List<Course> genCourseListByHtml(File file) throws IOException {
        //读取 html文件
        StringBuilder builder = new StringBuilder();
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            builder.append(line);
        }

        //格式化
        List<Course> courseList = new ArrayList<>();
        Document doc = org.jsoup.Jsoup.parse(builder.toString());
        Element table1 = doc.getElementById("StuCul_TimetableQry_TimeTable").getElementsByClass("WtbodyZlistS").get(0);
        Elements trs = table1.getElementsByTag("tr");
        for (Element tr : trs) {
            Elements tds = tr.getElementsByTag("td");
            int countDay = 1;
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
    private static @NonNull List<Course> parseCourseInfo(List<String> split, int countDay) {
        List<Course> resCourseList = new ArrayList<>();

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

        Matcher numMatcher = NUM_RANGE_REGEX.matcher(splitEnd.get(2));
        numMatcher.find();
        Integer startNode = Integer.parseInt(numMatcher.group(1));
        Integer endNode = Integer.parseInt(numMatcher.group(3));

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
                            new Course(
                                    splitEnd.get(0),
                                    countDay,
                                    splitEnd.get(4),
                                    teacher,
                                    startNode,
                                    endNode,
                                    Integer.parseInt(startWeek),
                                    Integer.parseInt(endWeek),
                                    String.join(";", split.subList(5, split.size()))
                            )
                    );
                }
            }
        } else {
            String teacher = splitEnd.get(1).substring(0, splitEnd.get(1).lastIndexOf('('));
            Matcher itMatcher = NUM_RANGE_REGEX.matcher(splitEnd.get(3));
            while (itMatcher.find()) {
                String startWeek = itMatcher.group(1).trim();
                String endWeek = (itMatcher.group(3) == null || "".equals(itMatcher.group(3).trim())) ? itMatcher.group(1) : itMatcher.group(3);
                resCourseList.add(
                        new Course(
                                splitEnd.get(0),
                                countDay,
                                splitEnd.get(4),
                                teacher,
                                startNode,
                                endNode,
                                Integer.parseInt(startWeek),
                                Integer.parseInt(endWeek),
                                String.join(";", split.subList(5, split.size()))
                        )
                );
            }
        }
        return resCourseList;
    }
}