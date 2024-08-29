package com.servxglobal.tms.batchservice.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MapperUtil {

    static ObjectMapper mapper = new ObjectMapper();

    public static <T> T readAsObject(Class<T> clazz, String value) throws Exception {
        try {
            return mapper.readValue(value, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
