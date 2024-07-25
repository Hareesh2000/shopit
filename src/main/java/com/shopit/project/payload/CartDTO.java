package com.shopit.project.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long cartId;
    private Double totalPrice = 0.0;
    private List<CartItemDTO> cartItems = new ArrayList<>();

//    public Double getTotalPrice() {
//        totalPrice = 0.0;
//        for(CartItemDTO cartItem : cartItems){
//            totalPrice += cartItem.getTotalPrice();
//        }
//        return totalPrice;
//    }
}
