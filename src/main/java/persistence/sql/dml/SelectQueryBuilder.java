package persistence.sql.dml;

import jakarta.persistence.Entity;

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

    private String joinQuery(Class<?> entityClass) {
        return "SELECT * FROM " + getTableName() +  + ";";
    }
}
