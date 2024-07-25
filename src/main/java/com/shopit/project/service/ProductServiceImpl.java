package com.shopit.project.service;

import com.shopit.project.exceptions.APIException;
import com.shopit.project.exceptions.ResourceNotFoundException;
import com.shopit.project.model.*;
import com.shopit.project.payload.ProductDTO;
import com.shopit.project.payload.ProductResponse;
import com.shopit.project.repository.CartItemRepository;
import com.shopit.project.repository.CartRepository;
import com.shopit.project.repository.CategoryRepository;
import com.shopit.project.repository.ProductRepository;
import com.shopit.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    ModelMapper modelMapper;
    CategoryRepository categoryRepository;
    ProductRepository productRepository;
    FileService fileService;
    CartService cartService;

    private final AuthUtil authUtil;

    @Value("${project.image}")
    private String path;

    @Autowired
    public ProductServiceImpl(CategoryRepository categoryRepository,
                              ProductRepository productRepository,
                              ModelMapper modelMapper,
                              FileService fileService, CartRepository cartRepository,
                              AuthUtil authUtil, CartItemRepository cartItemRepository,
                              CartService cartService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
        this.fileService = fileService;
        this.cartRepository = cartRepository;
        this.authUtil = authUtil;
        this.cartItemRepository = cartItemRepository;
        this.cartService = cartService;
    }

    private Double calculateSpecialPrice(Double productPrice, Double productDiscountPercentage) {
        return productPrice - ((productDiscountPercentage * 0.01) * productPrice);
    }

    private void setSpecialPrice(List<ProductDTO> productDTOS) {
        for (ProductDTO productDTO : productDTOS) {
            Double specialPrice = calculateSpecialPrice(productDTO.getProductPrice(),
                    productDTO.getProductDiscountPercentage());
            productDTO.setProductSpecialPrice(specialPrice);
        }
    }

    @Override
    public ProductResponse getProducts(Integer pageNumber, Integer pageSize,
                                       String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> productPage = productRepository.findByDeleteDateIsNull(pageDetails);
        List<Product> products = productPage.getContent();

        if(products.isEmpty()){
            throw new APIException("No Product exists for the request!!");
        }

        ProductResponse productResponse = new ProductResponse();

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        setSpecialPrice(productDTOS);

        productResponse.setContent(productDTOS);

        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());

        return productResponse;
    }


    @Override
    public ProductResponse getProductsByCategory(Long categoryId, Integer pageNumber, Integer pageSize,
                                                 String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category ID", categoryId));

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> productsByCategoryPage =
                productRepository.findByCategoryAndDeleteDateIsNull(category, pageDetails);

        List<Product> productsByCategory = productsByCategoryPage.getContent();

        if(productsByCategory.isEmpty()){
            throw new APIException("No Product exists in the category " + category.getCategoryName() +"for the request");
        }

        ProductResponse productResponse = new ProductResponse();

        List<ProductDTO> productByCategoryDTOS = productsByCategory.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        setSpecialPrice(productByCategoryDTOS);

        productResponse.setContent(productByCategoryDTOS);

        productResponse.setPageNumber(productsByCategoryPage.getNumber());
        productResponse.setPageSize(productsByCategoryPage.getSize());
        productResponse.setTotalElements(productsByCategoryPage.getTotalElements());
        productResponse.setTotalPages(productsByCategoryPage.getTotalPages());
        productResponse.setLastPage(productsByCategoryPage.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse getProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize,
                                                String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> productsByKeywordPage =
                productRepository.findByProductNameContainingIgnoreCaseAndDeleteDateIsNull(keyword, pageDetails);

        List<Product> productsByKeyword = productsByKeywordPage.getContent();

        if(productsByKeyword.isEmpty()){
            throw new APIException("No Product found with keyword " + keyword + "for the request");
        }

        ProductResponse productResponse = new ProductResponse();

        List<ProductDTO> productsByKeywordDTOS = productsByKeyword.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        setSpecialPrice(productsByKeywordDTOS);

        productResponse.setContent(productsByKeywordDTOS);

        productResponse.setPageNumber(productsByKeywordPage.getNumber());
        productResponse.setPageSize(productsByKeywordPage.getSize());
        productResponse.setTotalElements(productsByKeywordPage.getTotalElements());
        productResponse.setTotalPages(productsByKeywordPage.getTotalPages());
        productResponse.setLastPage(productsByKeywordPage.isLast());

        return productResponse;
    }


    @Override
    public ProductDTO addProduct(ProductDTO productDTO, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category ID", categoryId));

        List<Product> products = category.getProducts();

        for(Product product : products){
            if(productDTO.getProductName().equals(product.getProductName())){
                throw new APIException("Product already exists");
            }
        }

        Optional<Product> optionalProduct = productRepository.findByProductName(productDTO.getProductName());

        if(optionalProduct.isPresent()) {
            if(optionalProduct.get().getDeleteDate() == null)
                throw new APIException("Product with the name " + productDTO.getProductName() + " already exists");
            else
                throw new APIException("Product with the name " + productDTO.getProductName() + " has been deleted");
        }

        Product product = modelMapper.map(productDTO, Product.class);

        product.setProductImage("default.png");
        product.setCategory(category);

        Product productSaved = productRepository.save(product);

        ProductDTO productDTOSaved = modelMapper.map(productSaved, ProductDTO.class);

        Double specialPrice = calculateSpecialPrice(productSaved.getProductPrice(),
                productSaved.getProductDiscountPercentage());
        productDTOSaved.setProductSpecialPrice(specialPrice);

        return productDTOSaved;
    }

    @Override
    public ProductDTO permanentDeleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Product ID", productId));

        productRepository.delete(product);

        //        ToDo update Cart with deleted products with deleteProductFromCart

        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Product ID", productId));

        if(product.getDeleteDate() != null)
            throw new APIException("Product with the name " + product.getProductName() + " has already been deleted");

        product.setDeleteDate(new Date());

        Product savedProduct = productRepository.save(product);

        //        ToDo update Cart with deleted products with deleteProductFromCart

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO unDeleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Product ID", productId));

        if(product.getDeleteDate() == null)
            throw new APIException("Product with the name " + product.getProductName() + " has not been deleted");

        product.setDeleteDate(null);

        Product unDeletedProduct = productRepository.save(product);

        return modelMapper.map(unDeletedProduct, ProductDTO.class);
    }



    @Transactional
    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Product ID", productId));

        if(product.getDeleteDate() != null)
            throw new APIException("Product with the name " + product.getProductName() + " has been deleted");

        product.setProductName(productDTO.getProductName());
        product.setProductDescription(productDTO.getProductDescription());
        product.setProductQuantity(productDTO.getProductQuantity());

        double productPrice = productDTO.getProductPrice();
        double productDiscountPercentage = productDTO.getProductDiscountPercentage();

        product.setProductPrice(productPrice);
        product.setProductDiscountPercentage(productDiscountPercentage);

        List<CartItem> cartItems = product.getCartItems();
        //Set<Cart> modifiedCarts = new HashSet<>();
        for(CartItem cartItem : cartItems){
            cartItem.getProduct().setProductName(productDTO.getProductName());
            cartItem.getProduct().setProductDescription(productDTO.getProductDescription());
            cartItem.getProduct().setProductQuantity(productDTO.getProductQuantity());
            cartItem.getProduct().setProductPrice(productPrice);
            cartItem.getProduct().setProductDiscountPercentage(productDiscountPercentage);

            cartItem.setProductPrice(productPrice);
            cartItem.setProductDiscountPercentage(productDiscountPercentage);
            CartItem savedCartItem = cartItemRepository.save(cartItem);

//            modifiedCarts.add(savedCartItem.getCart());
        }

        Product savedProduct = productRepository.save(product);

//        ToDo update Cart with modified products with updateProductsInCart

//        for(Cart cart : modifiedCarts){
//            Double cartTotalPrice = 0.0;
//            for(CartItem cartItem : cart.getCartItems()){
//                cartTotalPrice = cartService.getCartItemTotalPrice(cartItem) + cartTotalPrice;
//            }
//            cart.setTotalPrice(cartTotalPrice);
//            System.out.println("Check2");
//            cartRepository.save(cart);
//        }


        ProductDTO productDTOSaved = modelMapper.map(savedProduct, ProductDTO.class);

        double specialPrice = calculateSpecialPrice(productPrice, productDiscountPercentage);
        productDTOSaved.setProductSpecialPrice(specialPrice);

        return productDTOSaved;
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Product ID", productId));

        String path = "images";
        String fileName = fileService.uploadFile(path, image);

        product.setProductImage(fileName);

        List<CartItem> cartItems = product.getCartItems();
        for(CartItem cartItem : cartItems){
            cartItem.getProduct().setProductImage(fileName);
        }

        Product updatedProduct = productRepository.save(product);

        return modelMapper.map(updatedProduct, ProductDTO.class);
    }


}
