package persistence.sql;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

public class TableColumn {
    private final Class<?> type;
    private final String name;
    private final boolean isNullable;
    private final boolean isForeignKey;
    private final String referencedTable;

    public TableColumn(Field column) {
        this.type = resolveType(column);
        this.name = resolveColumnName(column);
        this.isNullable = nullable(column);
        this.isForeignKey = column.isAnnotationPresent(ManyToOne.class);
        this.referencedTable = isForeignKey ? resolveReferencedTable(column) : null;
    }

    private Class<?> resolveType(Field field) {
        if (field.isAnnotationPresent(ManyToOne.class)) {
            return Long.class; // Foreign key type
        }
        return field.getType();
    }

    private String resolveReferencedTable(Field field) {
        Class<?> targetEntity;
        if (Collection.class.isAssignableFrom(field.getType())) {
            ParameterizedType type = (ParameterizedType) field.getGenericType();
            targetEntity = (Class<?>) type.getActualTypeArguments()[0];
        } else {
            targetEntity = field.getType();
        }
        return new TableMeta(targetEntity).tableName();
    }

    public Class<?> type() {
        return type;
    }

    public String name() {
        return name;
    }

    public boolean isNotNullable() {
        return !isNullable;
    }

    public boolean isForeignKey() {
        return isForeignKey;
    }

    public boolean isReferencedTable() {
        return referencedTable != null;
    }

    public boolean isCollection() {
        return Collection.class.isAssignableFrom(type);
    }

    public String referencedTable() {
        return referencedTable;
    }

    private boolean nullable(Field field) {
        Column column = field.getAnnotation(Column.class);
        return column == null || column.nullable();
    }

    private String resolveColumnName(Field field) {
        if (field.isAnnotationPresent(ManyToOne.class)) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            if (!joinColumn.name().isEmpty()) {
                return joinColumn.name();
            }
            return field.getName() + "_id";
        }
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            if (!column.name().isEmpty()) {
                return column.name();
            }
        }
        return field.getName();
    }
}
