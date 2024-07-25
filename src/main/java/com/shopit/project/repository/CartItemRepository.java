package com.shopit.project.repository;

import com.shopit.project.model.Cart;
import com.shopit.project.model.CartItem;
import com.shopit.project.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    CartItem findCartItemByProductAndCart(Product product, Cart cart);
}
