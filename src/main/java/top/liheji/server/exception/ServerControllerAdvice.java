package top.liheji.server.exception;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.liheji.server.constant.ErrorCodeEnum;
import top.liheji.server.util.R;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Galaxy
 * @time : 2023/1/12 21:17
 * @create : IdeaJ
 * @project : server
 * @description :
 */
@RestControllerAdvice(basePackages = "top.liheji.server.controller")
public class ServerControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException method) {
        BindingResult result = method.getBindingResult();
        Map<String, String> map = new HashMap<>(100);
        result.getFieldErrors().forEach((it) -> {
            map.put(it.getField(), it.getDefaultMessage());
        });
        return R.error(ErrorCodeEnum.VALID_EXCEPTION).put("data", map);
    }

    @ExceptionHandler(Throwable.class)
    public R handleThrowable(Throwable e) {
        e.printStackTrace();
        return R.error(e.getMessage());
    }
}
