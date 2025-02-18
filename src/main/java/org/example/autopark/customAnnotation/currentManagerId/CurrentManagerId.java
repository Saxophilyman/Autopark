package org.example.autopark.customAnnotation.currentManagerId;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentManagerId {
}

