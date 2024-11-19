package persistence.sql.dml;

import jakarta.persistence.*;
import persistence.sql.TableMeta;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

//todo Object Has Another Object with one to many annotation join table with join query
public class SelectQueryBuilder extends DMLQueryBuilder {
    public SelectQueryBuilder(Class<?> clazz) {
        super(clazz);
    }

    public String findAll(Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("This Class is not an Entity ");
        }

        String tableName = getTableName();
        return "SELECT * FROM " + tableName +joinQuery(entityClass)+ ";";
    }

    public String findById(Class<?> entityClass, Object id) {
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("This Class is not an Entity ");
        }
        String tableName = getTableName();
        return "SELECT * FROM " + tableName + " WHERE id = " + id + ";";
    }

    public String customSelect(Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("This Class is not an Entity ");
        }
        String tableName = getTableName();
        return "SELECT * FROM " + tableName +joinQuery(entityClass)+ ";";
    }

    private String joinQuery(Class<?> entityClass) {
        StringBuilder joinBuilder = new StringBuilder();
        System.out.println("entityClass = " + entityClass);
        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .filter(field -> field.getAnnotation(OneToMany.class).fetch() == FetchType.EAGER)
                .forEach(field -> {
                    // Get target entity type from collection
                    Class<?> targetEntity = getTargetEntityType(field);
                    TableMeta targetTableMeta = new TableMeta(targetEntity);

                    // Get join column name
                    String joinColumnName = getJoinColumnName(field);

                    // Build join clause
                    joinBuilder.append(" LEFT JOIN ")
                            .append(targetTableMeta.tableName())
                            .append(" ON ")
                            .append(getTableName())
                            .append(".id = ")
                            .append(targetTableMeta.tableName())
                            .append(".")
                            .append(joinColumnName);

                    // Add target table columns to select clause
                    joinBuilder.append(" SELECT ")
                            .append(targetTableMeta.tableColumn().stream()
                                    .map(column -> targetTableMeta.tableName() + "." + column.name())
                                    .collect(Collectors.joining(", ")));
                });

        return joinBuilder.toString();
    }

    private Class<?> getTargetEntityType(Field field) {
        if (Collection.class.isAssignableFrom(field.getType())) {
            ParameterizedType type = (ParameterizedType) field.getGenericType();
            return (Class<?>) type.getActualTypeArguments()[0];
        }
        return field.getType();
    }

    private String getJoinColumnName(Field field) {
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        if (joinColumn != null && !joinColumn.name().isEmpty()) {
            return joinColumn.name();
        }
        // Default naming strategy
        return field.getDeclaringClass().getSimpleName().toLowerCase() + "_id";
    }
}
