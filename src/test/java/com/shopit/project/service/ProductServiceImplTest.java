package com.shopit.project.service;

import com.shopit.project.model.CartItem;
import com.shopit.project.model.Category;
import com.shopit.project.model.Product;
import com.shopit.project.payload.ProductDTO;
import com.shopit.project.payload.ProductResponse;
import com.shopit.project.repository.CartItemRepository;
import com.shopit.project.repository.CategoryRepository;
import com.shopit.project.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private FileService fileService;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void testGetProducts() {
        List<Product> products = List.of(new Product(), new Product());
        Page<Product> productPage = new PageImpl<>(products);
        when(productRepository.findByDeleteDateIsNull(any(Pageable.class))).thenReturn(productPage);

        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductPrice(100.0);
        productDTO.setProductDiscountPercentage(10.0);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        ProductResponse productResponse = productService.getProducts(0, 10, "productName", "asc");

        assertEquals(2, productResponse.getContent().size());
        verify(productRepository).findByDeleteDateIsNull(any(Pageable.class));

        assertEquals(90.0, productResponse.getContent().getFirst().getProductSpecialPrice());
    }

    @Test
    void testGetProductsByCategory() {
        Category category = new Category();
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        List<Product> products = List.of(new Product(), new Product());
        Page<Product> productPage = new PageImpl<>(products);
        when(productRepository.findByCategoryAndDeleteDateIsNull(any(Category.class), any(Pageable.class)))
                .thenReturn(productPage);

        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductPrice(100.0);
        productDTO.setProductDiscountPercentage(10.0);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        ProductResponse productResponse = productService.getProductsByCategory(1L, 0, 10, "productName", "asc");

        assertEquals(2, productResponse.getContent().size());
        verify(categoryRepository).findById(anyLong());
        verify(productRepository).findByCategoryAndDeleteDateIsNull(any(Category.class), any(Pageable.class));

        assertEquals(90.0, productResponse.getContent().getFirst().getProductSpecialPrice());
    }

    @Test
    void testGetProductsByKeyword() {
        List<Product> products = List.of(new Product(), new Product());
        Page<Product> productPage = new PageImpl<>(products);
        when(productRepository.findByProductNameContainingIgnoreCaseAndDeleteDateIsNull(anyString(), any(Pageable.class)))
                .thenReturn(productPage);

        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductPrice(100.0);
        productDTO.setProductDiscountPercentage(10.0);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        ProductResponse productResponse = productService.getProductsByKeyword("keyword", 0, 10, "productName", "asc");

        assertEquals(2, productResponse.getContent().size());
        verify(productRepository).findByProductNameContainingIgnoreCaseAndDeleteDateIsNull(anyString(), any(Pageable.class));

        assertEquals(90.0, productResponse.getContent().getFirst().getProductSpecialPrice());
    }

    @Test
    void testAddProduct() {
        Category category = new Category();
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        Product product = new Product();
        product.setProductPrice(100.0);
        product.setProductDiscountPercentage(10.0);
        when(modelMapper.map(any(ProductDTO.class), eq(Product.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO productDTO = new ProductDTO();
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        ProductDTO result = productService.addProduct(productDTO, 1L);

        assertNotNull(result);
        verify(categoryRepository).findById(anyLong());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testPermanentDeleteProduct() {
        Product product = new Product();
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        ProductDTO productDTO = new ProductDTO();
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        ProductDTO result = productService.permanentDeleteProduct(1L);

        assertNotNull(result);
        verify(productRepository).findById(anyLong());
        verify(productRepository).delete(any(Product.class));
    }

    @Test
    void testDeleteProduct() {
        Product product = new Product();
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO productDTO = new ProductDTO();
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        ProductDTO result = productService.deleteProduct(1L);

        assertNotNull(result);
        verify(productRepository).findById(anyLong());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testUnDeleteProduct() {
        Product product = new Product();
        product.setDeleteDate(new Date());
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO productDTO = new ProductDTO();
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        ProductDTO result = productService.unDeleteProduct(1L);

        assertNotNull(result);
        verify(productRepository).findById(anyLong());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testUpdateProduct() {
        Product product = new Product();
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        List<CartItem> cartItems = List.of(cartItem);
        product.setCartItems(cartItems);

        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductPrice(100.0);
        productDTO.setProductDiscountPercentage(10.0);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        ProductDTO result = productService.updateProduct(productDTO, 1L);

        assertNotNull(result);
        verify(productRepository).findById(anyLong());
        verify(productRepository).save(any(Product.class));
        verify(cartItemRepository, times(cartItems.size())).save(any(CartItem.class));

        assertEquals(90.0, result.getProductSpecialPrice());
    }

    @Test
    void testUpdateProductImage() throws IOException {
        Product product = new Product();
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(fileService.uploadFile(anyString(), any(MultipartFile.class))).thenReturn("filename.png");

        ProductDTO productDTO = new ProductDTO();
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        ProductDTO result = productService.updateProductImage(1L, mock(MultipartFile.class));

        assertNotNull(result);
        verify(productRepository).findById(anyLong());
        verify(fileService).uploadFile(anyString(), any(MultipartFile.class));
        verify(productRepository).save(any(Product.class));
    }
}