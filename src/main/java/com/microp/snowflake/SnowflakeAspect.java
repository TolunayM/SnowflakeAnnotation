package com.microp.snowflake;

import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Table;
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

    // Cache for classes which don't have @Entity/@Table/@MappedSuperclass annotation
    private final Map<Class<?>, Boolean> nonEntityCache = new ConcurrentHashMap<>();

    public SnowflakeAspect(Snowflake snowflake) {
        this.snowflake = snowflake;
    }

    @Before("@within(org.springframework.stereotype.Service) || " +
            "@within(org.springframework.stereotype.Repository) || " +
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

        if (nonEntityCache.containsKey(clazz)) return;

        List<Field> snowflakeFields = snowflakeFieldCache.get(clazz);

        if (snowflakeFields == null) {
            // Check for @Entity/@Table/@MappedSuperclass annotation
            if (!isPersistentClass(clazz)) {
                nonEntityCache.put(clazz, true);
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

        //TODO This should be handled for different variable types maybe annotation can have optional settings for different variable types
        // most common one is using snowflake on string but it cause to performance issues
        for (Field field : snowflakeFields) {
            if (field.get(arg) == null) {
                if(field.getType() == Long.class){
                    field.set(arg, snowflake.nextId());
                }else{
                    //TODO snap this exception handling from here
                    throw new RuntimeException("SnowflakeId field type must be Long");
                }
//                field.set(arg, snowflake.nextId());
            }
        }
    }

    private boolean isPersistentClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Entity.class) ||
                clazz.isAnnotationPresent(Table.class) ||
                clazz.isAnnotationPresent(MappedSuperclass.class);
    }
}
