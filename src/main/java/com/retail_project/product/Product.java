package com.retail_project.product;

import com.retail_project.cartItem.CartItem;
import com.retail_project.category.Category;
import com.retail_project.inventory.Inventory;
import com.retail_project.orderItem.OrderItem;
import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
public class Product {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private String description;
    @Column(name = "image_url")
    private String imageUrl;
    private BigDecimal price;

    @OneToOne(mappedBy = "product",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, // Default is LAZY, but adding for clarity
            orphanRemoval = true)
    private Inventory inventory;

    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL)
    private List<CartItem> cartItems = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

}
