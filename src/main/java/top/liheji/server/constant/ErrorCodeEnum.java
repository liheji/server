package top.liheji.server.constant;

import lombok.Getter;

/**
 * @author : Galaxy
 * @time : 2023/1/12 21:33
 * @create : IdeaJ
 * @project : gulimall
 * @description :
 * <p>
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为5为数字
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：10000:通用 000:未知异常
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 * 10: 通用
 * 001：参数格式校验
 */
@Getter
public enum ErrorCodeEnum {
    UNKNOWN_EXCEPTION(10000, "未知异常"),
    VALID_EXCEPTION(10001, "参数校验错误");

    final int code;
    final String msg;

    ErrorCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
