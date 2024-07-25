package com.shopit.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "product_id")
    private Long productId;

    @NotBlank
    @Size(min = 2, message = "Product name must contain atleast 2 characters")
    @Column(unique = true)
    private String productName;

    private String productImage;

    @Size(min = 5, message = "Product name must contain atleast 5 characters")
    private String productDescription;
    private Integer productQuantity;
    private Double productPrice;
    private Double productDiscountPercentage;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User user;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    private Date deleteDate;

    public Double getSpecialPrice(){
        return productPrice - (productDiscountPercentage * 0.01 * productPrice);
    }
}
