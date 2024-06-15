package top.yilee.server.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : Galaxy
 * @time : 2021/12/29 14:39
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 课程表实体
 */
@Data
public class CourseVo implements Serializable {
    private static final long serialVersionUID = 1587842998002988039L;

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

    public CourseVo(String name, Integer day, String room,
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
