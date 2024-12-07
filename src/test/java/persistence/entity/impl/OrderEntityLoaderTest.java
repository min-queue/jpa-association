package persistence.entity.impl;

import domain.Order;
import domain.OrderItem;
import jdbc.JdbcTemplate;
import jdbc.RowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.sql.dml.SelectQueryBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderEntityLoaderTest {
    private Connection connection;
    private JdbcTemplate jdbcTemplate;
    private EntityLoader<Order> orderLoader;
    private EntityLoader<OrderItem> orderItemLoader;

    @BeforeEach
    void setUp() throws SQLException {
        // DB 연결
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        jdbcTemplate = new JdbcTemplate(connection);
        orderLoader = new EntityLoader<>(jdbcTemplate);
        orderItemLoader = new EntityLoader<>(jdbcTemplate);

        // 테이블 생성
        jdbcTemplate.execute("""
           CREATE TABLE IF NOT EXISTS orders (
               id BIGINT AUTO_INCREMENT PRIMARY KEY,
               order_number VARCHAR(255)
           );
       """);

        jdbcTemplate.execute("""
           CREATE TABLE IF NOT EXISTS order_items (
               id BIGINT AUTO_INCREMENT PRIMARY KEY,
               product VARCHAR(255),
               quantity INT,
               order_id BIGINT,
               FOREIGN KEY (order_id) REFERENCES orders(id)
           );
       """);

        // 테스트 데이터 삽입
        jdbcTemplate.execute("""
           INSERT INTO orders (id, order_number) VALUES 
           (1, 'ORD-001'),
           (2, 'ORD-002');
       """);

        jdbcTemplate.execute("""
           INSERT INTO order_items (id, product, quantity, order_id) VALUES 
           (1, 'Product 1', 2, 1),
           (2, 'Product 2', 1, 1),
           (3, 'Product 3', 3, 2);
       """);
    }

    @Test
    void testLoadOrder() {
        // Given
        Long orderId = 1L;

        // When
        Order loadedOrder = orderLoader.load(Order.class, orderId);
        // Then
        assertNotNull(loadedOrder);
        assertEquals(1L, loadedOrder.id());
        assertNotNull(loadedOrder.orderItems());
        assertEquals(2, loadedOrder.orderItems().size());
    }

    @Test
    void testLoadAllOrders() {
        // When
        List<Order> orders = orderLoader.loadAll(Order.class);

        // Then
        assertEquals(2, orders.size());
        assertNotNull(orders.get(0).orderItems());
        assertNotNull(orders.get(1).orderItems());
    }

    @Test
    void testLoadOrderItem() {
        // Given
        Long orderItemId = 1L;

        // When
        OrderItem loadedOrderItem = orderItemLoader.load(OrderItem.class, orderItemId);

        // Then
        assertNotNull(loadedOrderItem);
        assertEquals(1L, loadedOrderItem.id());
    }

    @Test
    void testLoadAllOrderItems() {
        // When
        List<OrderItem> orderItems = orderItemLoader.loadAll(OrderItem.class);

        // Then
        assertEquals(3, orderItems.size());
    }
}

class StubOrderJdbcTemplate extends JdbcTemplate {

    public StubOrderJdbcTemplate() {
        super(null);
    }

    @Override
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) {
        if (sql.contains("orders")) {
            OrderItem item1 = new OrderItem(1L, "Product 1", 2, 1L);
            OrderItem item2 = new OrderItem(2L, "Product 2", 1, 1L);
            Order order = new Order(1L, "ORD-001", Arrays.asList(item1, item2));
            return (T) order;
        } else if (sql.contains("order_items")) {
            OrderItem orderItem = new OrderItem(1L, "Product 1", 2, 1L);
            return (T) orderItem;
        }
        return null;
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        if (sql.contains("orders")) {
            List<Order> orders = new ArrayList<>();
            OrderItem item1 = new OrderItem(1L, "Product 1", 2, 1L);
            OrderItem item2 = new OrderItem(2L, "Product 2", 1, 1L);
            OrderItem item3 = new OrderItem(3L, "Product 3", 3, 2L);

            orders.add(new Order(1L, "ORD-001", Arrays.asList(item1, item2)));
            orders.add(new Order(2L, "ORD-002", Arrays.asList(item3)));
            return (List<T>) orders;
        } else if (sql.contains("order_items")) {
            List<OrderItem> orderItems = new ArrayList<>();
            orderItems.add(new OrderItem(1L, "Product 1", 2, 1L));
            orderItems.add(new OrderItem(2L, "Product 2", 1, 1L));
            orderItems.add(new OrderItem(3L, "Product 3", 3, 2L));
            return (List<T>) orderItems;
        }
        return null;
    }
}
