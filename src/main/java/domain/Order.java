package domain;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    @OneToMany(fetch = FetchType.EAGER)
    private List<OrderItem> orderItems;

    public Order(Long id, String orderNumber, List<OrderItem> orderItems) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.orderItems = orderItems;
    }

    public Order() {}

    public Long id(){
        return id;
    }

    public String orderNumber(){
        return orderNumber;
    }

    public List<OrderItem> orderItems(){
        return orderItems;
    }
}


