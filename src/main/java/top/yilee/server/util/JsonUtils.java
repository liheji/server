package top.yilee.server.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonUtils {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        // 配置日期格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 配置序列化和反序列化特性
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // 序列化方法
    public static String toJSONString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 序列化为 JSON 字节数组
    public static byte[] toJSONBytes(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 反序列化为 Map<String, Object>
    public static Map<String, Object> parseObject(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T parseObject(String jsonString, Class<T> clzz) {
        try {
            return objectMapper.readValue(jsonString, clzz);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 反序列化方法
    public static <T> T parseObject(String jsonString, TypeReference<T> reference) {
        try {
            return objectMapper.readValue(jsonString, reference);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 反序列化列表的方法
    public static <T> List<T> parseArray(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, new TypeReference<List<T>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}