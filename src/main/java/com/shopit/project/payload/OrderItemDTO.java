package com.shopit.project.payload;

import com.shopit.project.model.Order;
import com.shopit.project.model.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long orderItemId;
    private ProductDTO product;
    private Integer quantity;
    private Double productPrice;
    private Double productDiscountPercentage;

    public Double getTotalPrice(){
        return (productPrice - (productPrice * 0.01 * productDiscountPercentage)) * quantity;
    }

}
