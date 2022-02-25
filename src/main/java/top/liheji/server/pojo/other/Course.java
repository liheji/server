package top.liheji.server.pojo.other;

import lombok.Data;

/**
 * @Time : 2021/12/29 14:39
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : server
 * @Description :
 */

@Data
public class Course {
    /*
     * name 课程名
     * day 该课程的是星期几（7代表星期天）参数范围：1 - 7
     * room 教室
     * teacher 老师
     * startNode 开始为第几节课
     * endNode 结束时为第几节课
     * startWeek 开始周
     * endWeek 结束周
     * type 单双周，每周为0，单周为1，双周为2
     * credit = 0f 学分
     * note 备注
     * startTime 不规则的开始时间，长度必须为5，如"08:08"
     * endTime 不规则的结束时间，长度必须为5，如"08:08"
     */

    String name;
    Integer day;
    String room = "";
    String teacher = "";
    Integer startNode;
    Integer endNode;
    Integer startWeek;
    Integer endWeek;
    Integer type = 0;
    Float credit = 0f;
    String note = "";
    String startTime = "";
    String endTime = "";

    public Course(String name, Integer day, String room,
                  String teacher, Integer startNode, Integer endNode,
                  Integer startWeek, Integer endWeek, String note) {
        this.name = name;
        this.day = day;
        this.room = room;
        this.teacher = teacher;
        this.startNode = startNode;
        this.endNode = endNode;
        this.startWeek = startWeek;
        this.endWeek = endWeek;
        this.note = note;
    }
}
