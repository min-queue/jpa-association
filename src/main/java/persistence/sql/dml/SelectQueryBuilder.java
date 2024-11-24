package persistence.sql.dml;

import jakarta.persistence.*;
import persistence.sql.TableMeta;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;

//todo Object Has Another Object with one to many annotation join table with join query
public class SelectQueryBuilder extends DMLQueryBuilder {
    public SelectQueryBuilder(Class<?> clazz) {
        super(clazz);
    }

    public String findAll(Class<?> entityClass) {
        validatedEntity(entityClass.isAnnotationPresent(Entity.class));

        String tableName = getTableName();
        return "SELECT * FROM " + tableName + ";";
    }


    public String findById(Class<?> entityClass, Object id) {
        validatedEntity(entityClass.isAnnotationPresent(Entity.class));
        String tableName = getTableName();
        return "SELECT * FROM " + tableName + " WHERE id = " + id + ";";
    }

    public String customSelect(Class<?> entityClass) {
        validatedEntity(entityClass.isAnnotationPresent(Entity.class));
        String tableName = getTableName();
        StringBuilder query = new StringBuilder()
                .append("SELECT * FROM ")
                .append(tableName);

        String joinClause = buildJoinClause(entityClass);

        if (!joinClause.isEmpty()) {
            query.append(joinClause);
        }

        return "SELECT * FROM " + tableName + joinClause + ";";
    }

    private String buildJoinClause(Class<?> entityClass) {
        StringBuilder joins = new StringBuilder();

        Arrays.stream(entityClass.getDeclaredFields())
                .filter(this::isEagerOneToMany)
                .forEach(field -> {
                    Class<?> targetEntity = getTargetEntityType(field);
                    TableMeta targetTableMeta = new TableMeta(targetEntity);
                    String joinColumnName = getJoinColumnName(field);

                    joins.append(" LEFT JOIN ")
                            .append(targetTableMeta.tableName())
                            .append(" ON ")
                            .append(getTableName())
                            .append(".id = ")
                            .append(targetTableMeta.tableName())
                            .append(".")
                            .append(joinColumnName);
                });

        return joins.toString();
    }

    private boolean isEagerOneToMany(Field field) {
        if (!field.isAnnotationPresent(OneToMany.class)) {
            return false;
        }
        return field.getAnnotation(OneToMany.class).fetch() == FetchType.EAGER;
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

    private void validatedEntity(boolean entityClass) {
        if (!entityClass) {
            throw new IllegalArgumentException("This Class is not an Entity ");
        }
    }
}
