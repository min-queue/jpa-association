package persistence.sql.ddl;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.jetbrains.annotations.NotNull;
import persistence.sql.TableColumn;
import persistence.sql.TableId;
import persistence.sql.TableMeta;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CreateTableQueryBuilder extends DDLQueryBuilder {
    private static final String CREATE_TABLE = "CREATE TABLE ";

    public CreateTableQueryBuilder(Class<?> entityClass) {
        super(entityClass);
    }

    @Override
    public String executeQuery() {
        return createTable();
    }

    private String createTable() {
        List<TableColumn> tableColumns = tableMeta.tableColumn();
        TableId tableId = tableMeta.tableId();
        String idColumn = createIdColumn(tableId);
        String columns = createColumns(tableColumns);
        String foreignKeys = createForeignKeys(tableColumns);
        return CREATE_TABLE + tableMeta.tableName() + " (" + idColumn + columns +
                (foreignKeys.isEmpty() ? "" : ", " + foreignKeys) + ");";
    }

    private static String createColumns(List<TableColumn> tableColumns) {
        return tableColumns.stream()
                .filter(column -> !column.isForeignKey())
                .filter(column -> !column.isCollection())
                .map(column -> column.name() + " " +
                        H2DBDataType.castType(column.type()) +
                        (column.isNotNullable() ? " NOT NULL" : ""))
                .collect(Collectors.joining(", "));
    }

    private String createForeignKeys(List<TableColumn> tableColumns) {
        return tableColumns.stream()
                .filter(TableColumn::isForeignKey)
                .map(column -> column.name() + " BIGINT, " +
                        "FOREIGN KEY (" + column.name() + ") REFERENCES " +
                        column.referencedTable() + "(id)")
                .collect(Collectors.joining(", "));
    }

    @NotNull
    private static String createIdColumn(TableId tableId) {
        return tableId.name() + " " + H2DBDataType.castType(tableId.type()) +
                " PRIMARY KEY" + (tableId.isAutoIncrement() ? " AUTO_INCREMENT, " : ", ");
    }
}