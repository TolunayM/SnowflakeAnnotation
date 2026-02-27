package com.microp.snowflake;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class SnowflakeAspect {

    private final Snowflake snowflake;

    // Cache for annotation contained fields
    private final Map<Class<?>, List<Field>> snowflakeFieldCache = new ConcurrentHashMap<>();

    // Cache for classes which don't have @Snowflake annotation
    private final Map<Class<?>, Boolean> nonSnowCache = new ConcurrentHashMap<>();

    public SnowflakeAspect(Snowflake snowflake) {
        this.snowflake = snowflake;
    }

    @Before("@within(org.springframework.stereotype.Service) || " +
            "@within(org.springframework.stereotype.Component) || " +
            "@within(org.springframework.stereotype.Controller)")
    public void injectId(JoinPoint joinPoint) throws IllegalAccessException {

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg == null) continue;
            processEntity(arg);
        }
    }

    private void processEntity(Object arg) throws IllegalAccessException {
        Class<?> clazz = arg.getClass();

        if (nonSnowCache.containsKey(clazz)) return;

        List<Field> snowflakeFields = snowflakeFieldCache.get(clazz);

        if (snowflakeFields == null) {
            // Check for  annotation
            if (!isContainsSnow(clazz)) {
                nonSnowCache.put(clazz, true);
                return;
            }

            snowflakeFields = new ArrayList<>();
            Class<?> currentClass = clazz;

            // Searching for fields annotated with @SnowflakeId hierarchically
            while (currentClass != null && currentClass != Object.class) {
                for (Field field : currentClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(SnowflakeId.class)) {
                        field.setAccessible(true);
                        snowflakeFields.add(field);
                    }
                }
                currentClass = currentClass.getSuperclass();
            }

            snowflakeFieldCache.put(clazz, snowflakeFields);
        }

        // ID injection
        for (Field field : snowflakeFields) {
            if (field.get(arg) == null) {
                if(field.getType() == Long.class){
                    field.set(arg, snowflake.nextId());
                }else{
                    throw new RuntimeException("SnowflakeId field type must be Long");
                }
            }
        }
    }

    private boolean isContainsSnow(Class<?> clazz) {
        return clazz.isAnnotationPresent(SnowflakeData.class);
    }
}
