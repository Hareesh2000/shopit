package com.shopit.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "cart_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints =  {
        @UniqueConstraint(columnNames = {"cart_id", "product_id"})
})
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;
    private Double productPrice;
    private Double productDiscountPercentage;

}
