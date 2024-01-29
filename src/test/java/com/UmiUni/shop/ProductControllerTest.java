package com.UmiUni.shop;

import com.UmiUni.shop.controller.ProductController;
import com.UmiUni.shop.entity.Product;
import com.UmiUni.shop.service.ProductImageService;
import com.UmiUni.shop.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.given;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductImageService productImageService;

    @Test
    public void testCreateProduct() throws Exception {
        Product mockProduct = new Product();
        mockProduct.setProductName("Test Product");
        mockProduct.setDescription("Test Description");
        mockProduct.setPrice(BigDecimal.valueOf(1));
        mockProduct.setStockQuantity(100);

        ObjectMapper objectMapper = new ObjectMapper();
        String productJson = objectMapper.writeValueAsString(mockProduct);

        MockMultipartFile jsonFile = new MockMultipartFile("product", "", "application/json", productJson.getBytes());

        // Assuming no images for simplicity, but you could add mock images similarly
        MockMultipartFile[] images = new MockMultipartFile[]{
                // Example: Mocking a single image file
                new MockMultipartFile("images", "testImage1.jpg", "image/jpeg", "test image content 1".getBytes()),
                // Add more MockMultipartFile objects for additional images if needed
        };

        given(productService.createProduct(any(Product.class))).willReturn(mockProduct);

        MockMultipartHttpServletRequestBuilder builder = (MockMultipartHttpServletRequestBuilder) multipart("/api/products")
                .file(jsonFile)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE);

        // Add each image file to the request
        for (MockMultipartFile image : images) {
            builder.file(image);
        }

        mockMvc.perform(builder).andExpect(status().isOk());

    }

}
