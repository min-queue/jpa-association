package persistence;

import database.DatabaseServer;
import database.H2;
import domain.Order;
import domain.OrderItem;
import domain.Person;
import jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.entity.EntityManager;
import persistence.entity.impl.DefaultEntityManager;
import persistence.sql.ddl.CreateTableQueryBuilder;
import persistence.sql.ddl.DropTableQueryBuilder;
import persistence.sql.ddl.QueryBuilder;

import java.util.List;
import java.util.stream.Stream;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        logger.info("Starting application...");
        try {
            final DatabaseServer server = new H2();
            server.start();

            final JdbcTemplate jdbcTemplate = new JdbcTemplate(server.getConnection());
            createTables(jdbcTemplate);

            EntityManager entityManager = new DefaultEntityManager(jdbcTemplate);




            dropTables(jdbcTemplate);
            server.stop();
        } catch (Exception e) {
            logger.error("Error occurred", e);
        } finally {
            logger.info("Application finished");
        }
    }

    private static void createTables(JdbcTemplate jdbcTemplate) {
        List<String> createTableQuery = Stream.of(
                new CreateTableQueryBuilder(Person.class),
                new CreateTableQueryBuilder(Order.class),
                new CreateTableQueryBuilder(OrderItem.class)
        ).map(QueryBuilder::executeQuery).toList();

        createTableQuery.forEach(jdbcTemplate::execute);
    }

    private static void dropTables(JdbcTemplate jdbcTemplate) {
        List<String> dropTableQuery = Stream.of(
                new DropTableQueryBuilder(Person.class),
                new DropTableQueryBuilder(Order.class),
                new DropTableQueryBuilder(OrderItem.class)
        ).map(QueryBuilder::executeQuery).toList();
        dropTableQuery.forEach(jdbcTemplate::execute);
    }
}
