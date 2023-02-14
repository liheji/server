package top.liheji.server.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import top.liheji.server.constant.ErrorCodeEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Galaxy
 */
public class R extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    // 利用fastjson进行反序列化
    public Map<String, Object> getMap(String key) {
        Object data = get(key);
        String jsonString = JSON.toJSONString(data);
        return JSON.parseObject(jsonString);
    }

    public <T> List<T> getList(String key, Class<T> clzz) {
        Object data = get(key);
        String jsonString = JSON.toJSONString(data);
        return JSON.parseArray(jsonString, clzz);
    }

    public <T> T getObj(String key, Class<T> clzz) {
        Object data = get(key);
        String jsonString = JSON.toJSONString(data);
        return JSON.parseObject(jsonString, clzz);
    }

    public <T> T getObj(String key, TypeReference<T> reference) {
        Object data = get(key);
        String jsonString = JSON.toJSONString(data);
        return JSON.parseObject(jsonString, reference);
    }

    public R() {
        put("code", 0);
        put("msg", "success");
    }

    public static R error() {
        return error(1, "failure");
    }

    public static R error(String msg) {
        return error(1, msg);
    }

    public static R error(ErrorCodeEnum code) {
        return error(code.getCode(), code.getMsg());
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    public static R ok() {
        return new R();
    }

    @Override
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public Integer getCode() {
        return (Integer) this.get("code");
    }

    public Boolean isOk() {
        return (Integer) this.get("code") == 0;
    }

    public Boolean isError() {
        return (Integer) this.get("code") != 0;
    }

    public String toJSON() {
        return JSON.toJSONString(this);
    }
}
