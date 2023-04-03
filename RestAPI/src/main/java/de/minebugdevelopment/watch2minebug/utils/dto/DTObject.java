package de.minebugdevelopment.watch2minebug.utils.dto;

import de.minebugdevelopment.watch2minebug.entity.IEntity;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.proxy.HibernateProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
public abstract class DTObject {

    public DTObject parseFrom(@NonNull IEntity entity, String... skipFields) {
        mainLoop: for (Field dtoField : this.getClass().getDeclaredFields()) {
            if (Arrays.stream(skipFields).anyMatch(f -> f.equalsIgnoreCase(dtoField.getName()))) continue;
            try {
                dtoField.setAccessible(true);
                Field entityField;

                DTOFieldResolver dtoFieldResolver = dtoField.getDeclaredAnnotation(DTOFieldResolver.class);
                DTOMethodResolver dtoMethodResolver = dtoField.getDeclaredAnnotation(DTOMethodResolver.class);


                if (dtoFieldResolver != null) {

                    String[] path = dtoFieldResolver.value();

                    Field currentField;
                    Object reference = entity;

                    for (String s : path) {
                        if (reference instanceof HibernateProxy proxy) reference = proxy.getHibernateLazyInitializer().getImplementation();

                        currentField = reference.getClass().getDeclaredField(s);
                        currentField.setAccessible(true);
                        reference = currentField.get(reference);
                        if (reference == null) continue mainLoop;
                    }
                    dtoField.set(this, reference);

                } else if (dtoMethodResolver != null) {

                    entityField = entity.getClass().getDeclaredField(dtoMethodResolver.source());
                    entityField.setAccessible(true);

                    String methodeToGet = dtoMethodResolver.methode();
                    if (methodeToGet.length() == 0) methodeToGet = dtoField.getName();
                    Method parser = this.getClass().getDeclaredMethod(methodeToGet, Object.class);
                    parser.setAccessible(true);

                    if (entityField.get(entity) == null) continue;
                    Object result = parser.invoke(this, entityField.get(entity));
                    dtoField.set(this, result);

                } else {
                    entityField = entity.getClass().getDeclaredField(dtoField.getName());
                    entityField.setAccessible(true);
                    dtoField.set(this, entityField.get(entity));
                }
            } catch (NoSuchFieldException ex) {
                log.warn("NoSuchField: " + ex.getMessage());
            } catch (ReflectiveOperationException ex) {
                throw new DTOResolveException(ex);
            }
        }
        return this;
    }

    /**
     * It takes a DTO and appends its values to an entity
     *
     * @param entity The entity to append the DTO to.
     */
    public void appendTo(@NonNull IEntity entity) {
        try {
            for (Field dtoField : this.getClass().getDeclaredFields()) {

                dtoField.setAccessible(true);
                if (dtoField.get(this) == null) continue; // Skipping empty fields

                Optional<Field> optionalField = Arrays.stream(entity.getClass().getDeclaredFields())
                        .filter(f -> f.getName().equals(dtoField.getName()))
                        .findFirst();

                if (optionalField.isPresent()) {
                    Field entityField = optionalField.get();
                    entityField.setAccessible(true);

                    entityField.set(entity, dtoField.get(this));
                }
            }
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }


}
