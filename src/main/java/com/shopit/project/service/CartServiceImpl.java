package com.shopit.project.service;

import com.shopit.project.exceptions.APIException;
import com.shopit.project.exceptions.ResourceNotFoundException;
import com.shopit.project.model.Cart;
import com.shopit.project.model.CartItem;
import com.shopit.project.model.Product;
import com.shopit.project.model.User;
import com.shopit.project.payload.CartDTO;
import com.shopit.project.payload.CartItemDTO;
import com.shopit.project.repository.CartItemRepository;
import com.shopit.project.repository.CartRepository;
import com.shopit.project.repository.ProductRepository;
import com.shopit.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService{

    private final ModelMapper modelMapper;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    private final ProductRepository productRepository;

    private final AuthUtil authUtil;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, ProductRepository productRepository,
                           CartItemRepository cartItemRepository, ModelMapper modelMapper,
                           AuthUtil authUtil) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.modelMapper = modelMapper;
        this.authUtil = authUtil;
    }

//    private Cart createCart(User user){
//        Cart cart = new Cart();
//        cart.setTotalPrice(0.00);
//        cart.setUser(user);
//        Cart savedCart = cartRepository.save(cart);
//
//        return savedCart;
//    }

    @Override
    public Double getCartItemTotalPrice(CartItem cartItem) {
        return (cartItem.getProductPrice() -
                (cartItem.getProductDiscountPercentage() * 0.01 * cartItem.getProductPrice()))
                * cartItem.getQuantity();
    }

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Product ID", productId));

        if(product.getDeleteDate() != null)
            throw new APIException("Product with the name " + product.getProductName() + " has been deleted");

        User user = authUtil.loggedInUser();
        Cart cart = cartRepository.findByUser(user);

        CartItem cartItem = cartItemRepository.findCartItemByProductAndCart(product, cart);

        if(cartItem != null){
            throw new APIException("Product with the name " + product.getProductName() + " has been added to the cart");
        }

        if(product.getProductQuantity() == 0){
            throw new APIException("Product with the name " + product.getProductName() + " is out of stock");
        }

        if(product.getProductQuantity() < quantity){
            throw new APIException("Product with the name " + product.getProductName() +
                    " is not available for the requested quantity " + quantity +
                    ", available quantity is " + product.getProductQuantity());
        }

        CartItem newCartItem = new CartItem();

        newCartItem.setCart(cart);
        newCartItem.setProduct(product);
        newCartItem.setQuantity(quantity);
        newCartItem.setProductPrice(product.getProductPrice());
        newCartItem.setProductDiscountPercentage(product.getProductDiscountPercentage());

        cartItemRepository.save(newCartItem);

        List<CartItem> cartItems = cart.getCartItems();
        cartItems.add(newCartItem);

        cart.setCartItems(cartItems);
        cart.setTotalPrice(cart.getTotalPrice() + ( product.getSpecialPrice() * quantity));
        cartRepository.save(cart);

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItemDTO> cartItemDTOS = cartItems.stream()
                        .map(item -> modelMapper.map(item, CartItemDTO.class))
                        .toList();

        for(CartItemDTO cartItemDTO : cartItemDTOS){
            cartItemDTO.setTotalPrice(cartItemDTO.getTotalPrice());
            cartItemDTO.getProduct().setProductSpecialPrice(cartItemDTO.getProduct().getProductSpecialPrice());
        }
        cartDTO.setCartItems(cartItemDTOS);

        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        if(carts.isEmpty())
            throw new APIException("No cart exists!");

        List<CartDTO> cartDTOS = carts.stream()
                .map(cart -> modelMapper.map(cart, CartDTO.class))
                .toList();

        return cartDTOS;
    }

    @Override
    public CartDTO getUserCart() {
        User user = authUtil.loggedInUser();
        Cart cart = cartRepository.findByUser(user);

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        return cartDTO;
    }

    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Product ID", productId));

        if(product.getDeleteDate() != null)
            throw new APIException("Product with the name " + product.getProductName() + " has been deleted");

        User user = authUtil.loggedInUser();
        Cart cart = cartRepository.findByUser(user);

        if(product.getProductQuantity() == 0){
            throw new APIException("Product with the name " + product.getProductName() + " is out of stock");
        }

        if(product.getProductQuantity() < quantity){
            throw new APIException("Product with the name " + product.getProductName() +
                    "is not available for the requested quantity, available quantity is " + product.getProductQuantity());
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductAndCart(product, cart);
        if(cartItem == null){
            throw new APIException("Product with name " + product.getProductName() +
                    " is not added to the cart");
        }
        Double oldTotalPrice = getCartItemTotalPrice(cartItem);

        cartItem.setQuantity(quantity);

        cartItemRepository.save(cartItem);

        List<CartItem> cartItems = cart.getCartItems();
        for(CartItem item : cartItems){
            if(item.getCartItemId() == cartItem.getCartItemId()){
                item.setQuantity(quantity);
                if(quantity == 0){
                    cartItemRepository.delete(item);
                    cartItems.remove(item);
                }
            }
        }

        cart.setCartItems(cartItems);
        cart.setTotalPrice(cart.getTotalPrice() + ( product.getSpecialPrice() * quantity ) - oldTotalPrice);
        cartRepository.save(cart);

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItemDTO> cartItemDTOS = cartItems.stream()
                .map(item -> modelMapper.map(item, CartItemDTO.class))
                .toList();

        for(CartItemDTO cartItemDTO : cartItemDTOS){
            cartItemDTO.setTotalPrice(cartItemDTO.getTotalPrice());
            cartItemDTO.getProduct().setProductSpecialPrice(cartItemDTO.getProduct().getProductSpecialPrice());
        }
        cartDTO.setCartItems(cartItemDTOS);

        return cartDTO;
    }

    @Override
    public CartDTO deleteProductFromCart(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Product ID", productId));

        if(product.getDeleteDate() != null)
            throw new APIException("Product with the name " + product.getProductName() + " has been deleted");

        User user = authUtil.loggedInUser();
        Cart cart = cartRepository.findByUser(user);

        CartItem cartItem = cartItemRepository.findCartItemByProductAndCart(product, cart);

        if(cartItem == null){
            throw new APIException("Product with name " + product.getProductName() +
                    " is not added to the cart");
        }

        cart.setTotalPrice(cart.getTotalPrice() - getCartItemTotalPrice(cartItem));

        cart.getCartItems().remove(cartItem);

        Cart savedCart = cartRepository.save(cart); // cartItem deleted since orphanRemoval = true


        CartDTO cartDTO = modelMapper.map(savedCart, CartDTO.class);

        List<CartItemDTO> cartItemDTOS = cart.getCartItems().stream()
                .map(item -> modelMapper.map(item, CartItemDTO.class))
                .toList();
        cartDTO.setCartItems(cartItemDTOS);

        return cartDTO;

        }



}

