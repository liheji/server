package top.yilee.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import top.yilee.server.util.WebUtils;

/**
 * @author : Galaxy
 * @time : 2023/1/12 21:17
 * @create : IdeaJ
 * @project : server
 * @description :
 */
@ControllerAdvice
public class SpringControllerAdvice {
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> handleThrowable(Throwable e) {
        e.printStackTrace();

        String respHtml = WebUtils.redirectHtml(e.getMessage());
        if (e instanceof NoHandlerFoundException) {
            return new ResponseEntity<>(respHtml, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(respHtml, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
