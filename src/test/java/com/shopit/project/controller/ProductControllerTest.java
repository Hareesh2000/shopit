package com.shopit.project.controller;

import com.shopit.project.payload.ProductDTO;
import com.shopit.project.payload.ProductResponse;
import com.shopit.project.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    void testGetProducts() throws Exception {
        ProductResponse productResponse = new ProductResponse();
        when(productService.getProducts(anyInt(), anyInt(), anyString(), anyString())).thenReturn(productResponse);

        mockMvc.perform(get("/api/public/products")
                        .param("pageNumber", "1")
                        .param("pageSize", "10")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));

        verify(productService, times(1)).getProducts(anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    void testGetProductsByCategory() throws Exception {
        ProductResponse productResponse = new ProductResponse();
        when(productService.getProductsByCategory(anyLong(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(productResponse);

        mockMvc.perform(get("/api/public/categories/1/products")
                        .param("pageNumber", "1")
                        .param("pageSize", "10")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));

        verify(productService, times(1)).getProductsByCategory(anyLong(), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    void testGetProductsByKeyword() throws Exception {
        ProductResponse productResponse = new ProductResponse();
        when(productService.getProductsByKeyword(anyString(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(productResponse);

        mockMvc.perform(get("/api/public/keyword/phone/products")
                        .param("pageNumber", "1")
                        .param("pageSize", "10")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));

        verify(productService, times(1)).getProductsByKeyword(anyString(), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    void testAddProduct() throws Exception {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductPrice(100.0);
        productDTO.setProductDiscountPercentage(10.0);
        when(productService.addProduct(any(ProductDTO.class), anyLong())).thenReturn(productDTO);

        mockMvc.perform(post("/api/admin/categories/1/product")
                        .contentType("application/json")
                        .content("{\"productName\":\"Phone\"}"))
                .andExpect(status().isCreated());

        verify(productService, times(1)).addProduct(any(ProductDTO.class), anyLong());
    }

    @Test
    void testPermanentDeleteProduct() throws Exception {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductPrice(100.0);
        productDTO.setProductDiscountPercentage(10.0);
        when(productService.permanentDeleteProduct(anyLong())).thenReturn(productDTO);

        mockMvc.perform(delete("/api/admin/products/1/permanent"))
                .andExpect(status().isOk());

        verify(productService, times(1)).permanentDeleteProduct(anyLong());
    }

    @Test
    void testDeleteProduct() throws Exception {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductPrice(100.0);
        productDTO.setProductDiscountPercentage(10.0);
        when(productService.deleteProduct(anyLong())).thenReturn(productDTO);

        mockMvc.perform(delete("/api/admin/products/1"))
                .andExpect(status().isOk());

        verify(productService, times(1)).deleteProduct(anyLong());
    }

    @Test
    void testUnDeleteProduct() throws Exception {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductPrice(100.0);
        productDTO.setProductDiscountPercentage(10.0);
        when(productService.unDeleteProduct(anyLong())).thenReturn(productDTO);

        mockMvc.perform(put("/api/admin/products/1/undelete"))
                .andExpect(status().isOk());

        verify(productService, times(1)).unDeleteProduct(anyLong());
    }

    @Test
    void testUpdateProduct() throws Exception {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductPrice(100.0);
        productDTO.setProductDiscountPercentage(10.0);
        when(productService.updateProduct(any(ProductDTO.class), anyLong())).thenReturn(productDTO);

        mockMvc.perform(put("/api/admin/products/1")
                        .contentType("application/json")
                        .content("{\"productName\":\"Updated Phone\"}"))
                .andExpect(status().isOk());

        verify(productService, times(1)).updateProduct(any(ProductDTO.class), anyLong());
    }

    @Test
    void testUpdateProductImage() throws Exception {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductPrice(100.0);
        productDTO.setProductDiscountPercentage(10.0);
        when(productService.updateProductImage(anyLong(), any(MultipartFile.class))).thenReturn(productDTO);

        MockMultipartFile file = new MockMultipartFile("image", "image.jpg", "image/jpeg", "image data".getBytes());

        mockMvc.perform(multipart("/api/products/{productId}/image",1)
                        .file(file)
                        .with(request -> { // Set HTTP method to PUT
                                request.setMethod("PUT");
                                return request;
                         })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                        .andDo(MockMvcResultHandlers.print())
                        .andExpect(status().isOk());

        verify(productService, times(1)).updateProductImage(anyLong(), any(MultipartFile.class));
    }
}