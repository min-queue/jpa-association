package persistence.entity.impl;


import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jdbc.JdbcTemplate;
import persistence.entity.EntityRowMapper;
import persistence.sql.dml.SelectQueryBuilder;

import java.lang.reflect.Field;
import java.util.List;


public class EntityLoader<T> {
    private final JdbcTemplate jdbcTemplate;

    public EntityLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public T load(Class<T> clazz, Long id) {
        try {
            if (hasEagerRelation(clazz)){
                return jdbcTemplate.queryForObject(new SelectQueryBuilder(clazz).customSelect(clazz), new EntityRowMapper<>(clazz));
            }
            SelectQueryBuilder selectQueryBuilder = new SelectQueryBuilder(clazz);
            return jdbcTemplate.queryForObject(selectQueryBuilder.findById(clazz, id), new EntityRowMapper<>(clazz));
        } catch (RuntimeException e) {
            return null;
        }
    }

    public List<T> loadAll(Class<T> clazz) {
        try {

            SelectQueryBuilder selectQueryBuilder = new SelectQueryBuilder(clazz);
            return jdbcTemplate.query(selectQueryBuilder.findAll(clazz), new EntityRowMapper<>(clazz));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void loadRelations(T entity) {
        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(OneToMany.class)) {
                OneToMany oneToMany = field.getAnnotation(OneToMany.class);
                if (oneToMany.fetch() == FetchType.EAGER) {
                    loadEagerRelation(entity, field);
                } else {
//                    setupLazyRelation(entity, field);
                }
            }
        }
    }

    private boolean hasEagerRelation(Class<T> entity) {
        for (Field field : entity.getDeclaredFields()) {
            if (field.isAnnotationPresent(OneToMany.class)&& isEager(field)) {
                return true;
            }
        }
        return false;
    }

    boolean isEager(Field field) {
        return field.getAnnotation(OneToMany.class).fetch() == FetchType.EAGER;
    }

    private void loadEagerRelation(T entity, Field field) {

    }

    private void setupLazyRelation(T entity, Field field) {
        // Lazy 로딩 설정
    }
}
