package com.elevn.mes.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class JSONTools {
    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * 任何Java对象转换为JSON字符串
     * @param obj
     * @return
     */
    public static String object2json(Object obj){
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * JSON转换为对应的对象
     * @param json
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T> T json2object(String json,Class<T> clazz){
        try {
            return mapper.readValue(json,clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * JSON转集合
     * @param json
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T> List<T> json2list(String json, Class<T> clazz){
        try {
            return mapper.readValue(json, new TypeReference<List<T>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
