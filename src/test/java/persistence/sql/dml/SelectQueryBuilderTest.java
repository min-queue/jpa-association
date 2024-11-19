package persistence.sql.dml;

import domain.Order;
import domain.Person;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class SelectQueryBuilderTest {
    @Test
    void findAll() {
        SelectQueryBuilder selectQueryBuilder = new SelectQueryBuilder(Person.class);
        String expectedQuery = "SELECT * FROM users;";
        String actualQuery = selectQueryBuilder.findAll(Person.class);

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    void findById() {
        SelectQueryBuilder selectQueryBuilder = new SelectQueryBuilder(Person.class);
        String expectedQuery = "SELECT * FROM users WHERE id = 1;";
        String actualQuery = selectQueryBuilder.findById(Person.class, 1L);

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    void joinQuery() {
        SelectQueryBuilder selectQueryBuilder = new SelectQueryBuilder(Order.class);
        String expectedQuery = "SELECT * FROM orders LEFT JOIN order_items ON orders.id = order_items.order_id SELECT order_items.product, order_items.quantity, order_items.orderId;";
        String actualQuery = selectQueryBuilder.customSelect(Order.class);

        assertEquals(expectedQuery, actualQuery);
    }
}
