package de.minebugdevelopment.watch2minebug.utils.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface DTOMethodResolver {

    /**
     * Sets the variable from where it should get the value
     * then its sending this variable as parameter to a methode that has the same name as the current field
     */
    String source();

    /**
     * The methode to wich the source parameter should be passed
     * If not set it calls a methode with the same name as this field
     *
     * Note: The methode must have an Object parameter
     */
    String methode() default "";
}
