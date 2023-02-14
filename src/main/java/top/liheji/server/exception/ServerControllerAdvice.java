package top.liheji.server.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.liheji.server.util.R;

/**
 * @author : Galaxy
 * @time : 2023/1/12 21:17
 * @create : IdeaJ
 * @project : server
 * @description :
 */
@RestControllerAdvice(basePackages = "top.liheji.server.controller")
public class ServerControllerAdvice {

    @ExceptionHandler(Throwable.class)
    public R handleThrowable(Throwable e) {
        e.printStackTrace();
        return R.error(e.getMessage());
    }
}
