package com.meng.tools;

import com.google.gson.*;

import java.lang.reflect.*;

public class GSON {
    private static Gson gson = new Gson();

    public static <T> T fromJson(String json, Class<T> clz) {
        try {
            return (T) gson.fromJson(json, clz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T fromJson(String json, Type t) {
        return (T) gson.fromJson(json, t);
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
}
