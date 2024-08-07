package com.example.javautils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class CompareDiffClassUtils {

    // Cache
    private static Map<Class, Method[]> class2MethodCache = new ConcurrentHashMap<>();

    public static boolean compareDiffClass(Object o1, Object o2, boolean justCompareBothPresent) {
        if (o1 == null || o2 == null) {
            if(o1 == null && o2 == null) {
                return true;
            }
            return false;
        }
        Map<String, Object> name2Value = getName2Value(o1);
        Map<String, Object> name2Value2 = getName2Value(o2);
        for (Map.Entry<String, Object> stringObjectEntry : name2Value.entrySet()) {
            String name1 = stringObjectEntry.getKey();
            Object value2 = name2Value2.get(name1);
            if(value2 == null && !justCompareBothPresent) {
                return false;
            }
            if(!Objects.equals(value2, stringObjectEntry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static Map<String, Object> getName2Value(Object o) {

        Class<?> aClass = o.getClass();
        Method[] methods;

        // 从缓存中取
        if(class2MethodCache.containsKey(aClass)) {
            methods = class2MethodCache.get(aClass);
        }else {
            methods = getMethod(aClass);
            class2MethodCache.put(aClass, methods);
        }

        // 采用Maps.newHashMapWithExpectedSize(methods.length)会更好
        Map<String, Object> map = new HashMap<>();

        for (Method method : methods) {
            String name = method.getName();
            try {
                Object value = method.invoke(o);
                map.put(name.substring(3), value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    private static Method[] getMethod(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.getName().startsWith("get") && method.getParameterCount() == 0)
                .toArray(Method[]::new);
    }
}
