package org.jclass.jdbc;

import java.lang.annotation.*;

/**
 * Created by hasee on 2017/5/14.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LsAnnotation {
    String tableName() default "";
    
    String id() default "id";
}
