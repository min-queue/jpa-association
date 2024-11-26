package domain;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String product;

    private Integer quantity;

    @JoinColumn(name = "order_id")
    private Long orderId;

    public OrderItem(Long id, String product, Integer quantity, Long orderId) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.orderId = orderId;
    }

    public OrderItem() {}

    public Long id(){
        return id;
    }
    public String product(){
        return product;
    }
    public Integer quantity(){
        return quantity;
    }
}
