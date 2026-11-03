package com.myshopycart.productservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myshopycart.productservice.dto.AddProductImageRequest;
import com.myshopycart.productservice.dto.CreateProductRequest;
import com.myshopycart.productservice.entity.Product;
import com.myshopycart.productservice.service.ProductService;
import com.myshopycart.productservice.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductCrudControllerTest {

    private ProductService productService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        productService = mock(ProductService.class);

        ProductCrudController controller =
                new ProductCrudController(productService);

        PageableHandlerMethodArgumentResolver pageableResolver =
                new PageableHandlerMethodArgumentResolver();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
		.setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(pageableResolver)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreateProduct() throws Exception {

        CreateProductRequest request =
                new CreateProductRequest();

        request.setName("Dell Laptop");
        request.setDescription("Business laptop");
        request.setPrice(new BigDecimal("75000"));
        request.setCategoryId("electronics");
        request.setDistributorId("2");

        Product createdProduct =
                new Product();

        createdProduct.setId("product-1");
        createdProduct.setName("Dell Laptop");
        createdProduct.setDescription("Business laptop");
        createdProduct.setPrice(new BigDecimal("75000"));
        createdProduct.setCategoryId("electronics");
        createdProduct.setDistributorId("2");

        when(productService.createProduct(any(Product.class)))
                .thenReturn(createdProduct);

        mockMvc.perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value("product-1"))
                .andExpect(jsonPath("$.name")
                        .value("Dell Laptop"))
                .andExpect(jsonPath("$.price")
                        .value(75000));

        verify(productService)
                .createProduct(any(Product.class));
    }

    @Test
    void shouldGetAllProducts() throws Exception {

        Product product =
                new Product();

        product.setId("product-1");
        product.setName("Dell Laptop");
        product.setPrice(new BigDecimal("75000"));

        Page<Product> page =
                new PageImpl<>(
                        List.of(product),
                        PageRequest.of(0, 10),
                        1
                );

        when(productService.getAllProducts(any()))
                .thenReturn(page);

        mockMvc.perform(
                        get("/api/products")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id")
                        .value("product-1"))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Dell Laptop"));

        verify(productService)
                .getAllProducts(any());
    }

    @Test
    void shouldGetProductById() throws Exception {

        Product product =
                new Product();

        product.setId("product-1");
        product.setName("Dell Laptop");
        product.setPrice(new BigDecimal("75000"));

        when(productService.getProductById("product-1"))
                .thenReturn(product);

        mockMvc.perform(
                        get("/api/products/product-1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value("product-1"))
                .andExpect(jsonPath("$.name")
                        .value("Dell Laptop"));

        verify(productService)
                .getProductById("product-1");
    }

    @Test
    void shouldGetProductsByCategory() throws Exception {

        Product product =
                new Product();

        product.setId("product-1");
        product.setName("Dell Laptop");
        product.setCategoryId("electronics");

        Page<Product> page =
                new PageImpl<>(
                        List.of(product),
                        PageRequest.of(0, 10),
                        1
                );

        when(productService.getProductsByCategory(
                eq("electronics"),
                any()))
                .thenReturn(page);

        mockMvc.perform(
                        get("/api/products/category/electronics")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].categoryId")
                        .value("electronics"));

        verify(productService)
                .getProductsByCategory(
                        eq("electronics"),
                        any()
                );
    }

    @Test
    void shouldSearchProducts() throws Exception {

        Product product =
                new Product();

        product.setId("product-1");
        product.setName("Dell Laptop");

        Page<Product> page =
                new PageImpl<>(
                        List.of(product),
                        PageRequest.of(0, 10),
                        1
                );

        when(productService.searchProducts(
                eq("laptop"),
                any()))
                .thenReturn(page);

        mockMvc.perform(
                        get("/api/products/search")
                                .param("keyword", "laptop")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name")
                        .value("Dell Laptop"));

        verify(productService)
                .searchProducts(
                        eq("laptop"),
                        any()
                );
    }

    @Test
    void shouldUpdateProduct() throws Exception {

        Product updatedProduct =
                new Product();

        updatedProduct.setId("product-1");
        updatedProduct.setName("Updated Laptop");
        updatedProduct.setPrice(new BigDecimal("80000"));

        when(productService.updateProduct(
                eq("product-1"),
                any(Product.class)))
                .thenReturn(updatedProduct);

        Product request =
                new Product();

        request.setName("Updated Laptop");
        request.setPrice(new BigDecimal("80000"));

        mockMvc.perform(
                        put("/api/products/product-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value("product-1"))
                .andExpect(jsonPath("$.name")
                        .value("Updated Laptop"));

        verify(productService)
                .updateProduct(
                        eq("product-1"),
                        any(Product.class)
                );
    }

    @Test
    void shouldDeleteProduct() throws Exception {

        doNothing()
                .when(productService)
                .deleteProduct("product-1");

        mockMvc.perform(
                        delete("/api/products/product-1")
                )
                .andExpect(status().isNoContent());

        verify(productService)
                .deleteProduct("product-1");
    }

    @Test
    void shouldAddProductImage() throws Exception {

        AddProductImageRequest request =
                new AddProductImageRequest();

        request.setImageUrl(
                "https://example.com/laptop.jpg"
        );

        Product updatedProduct =
                new Product();

        updatedProduct.setId("product-1");
        updatedProduct.setName("Dell Laptop");
        updatedProduct.setImageUrls(
                List.of(
                        "https://example.com/laptop.jpg"
                )
        );

        when(productService.addProductImage(
                eq("product-1"),
                eq("https://example.com/laptop.jpg")))
                .thenReturn(updatedProduct);

        mockMvc.perform(
                        post("/api/products/product-1/images")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrls[0]")
                        .value("https://example.com/laptop.jpg"));

        verify(productService)
                .addProductImage(
                        eq("product-1"),
                        eq("https://example.com/laptop.jpg")
                );
    }

    @Test
    void shouldRemoveProductImage() throws Exception {

        AddProductImageRequest request =
                new AddProductImageRequest();

        request.setImageUrl(
                "https://example.com/laptop.jpg"
        );

        Product updatedProduct =
                new Product();

        updatedProduct.setId("product-1");
        updatedProduct.setName("Dell Laptop");
        updatedProduct.setImageUrls(
                List.of()
        );

        when(productService.removeProductImage(
                eq("product-1"),
                eq("https://example.com/laptop.jpg")))
                .thenReturn(updatedProduct);

        mockMvc.perform(
                        delete("/api/products/product-1/images")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrls")
                        .isEmpty());

        verify(productService)
                .removeProductImage(
                        eq("product-1"),
                        eq("https://example.com/laptop.jpg")
                );
    }

    @Test
    void shouldRejectProductWhenNameIsMissing() throws Exception {

        CreateProductRequest request =
                new CreateProductRequest();

        request.setDescription("Business laptop");
        request.setPrice(new BigDecimal("75000"));
        request.setCategoryId("electronics");
        request.setDistributorId("2");

        mockMvc.perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation Failed"))
                .andExpect(jsonPath("$.message")
                        .value("name: Product name is required"))
                .andExpect(jsonPath("$.path")
                        .value("/api/products"));

        verifyNoInteractions(productService);
    }

    @Test
    void shouldRejectProductWhenPriceIsInvalid() throws Exception {

        CreateProductRequest request =
                new CreateProductRequest();

        request.setName("Dell Laptop");
        request.setDescription("Business laptop");
        request.setPrice(BigDecimal.ZERO);
        request.setCategoryId("electronics");
        request.setDistributorId("2");

        mockMvc.perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation Failed"))
                .andExpect(jsonPath("$.message")
                        .value("price: Product price must be greater than 0"))
                .andExpect(jsonPath("$.path")
                        .value("/api/products"));

        verifyNoInteractions(productService);
    }

    @Test
    void shouldRejectImageWhenUrlIsMissing() throws Exception {

        AddProductImageRequest request =
                new AddProductImageRequest();

        mockMvc.perform(
                        post("/api/products/product-1/images")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation Failed"))
                .andExpect(jsonPath("$.message")
                        .value("imageUrl: Image URL is required"))
                .andExpect(jsonPath("$.path")
                        .value("/api/products/product-1/images"));

        verifyNoInteractions(productService);
    }

    @Test
    void shouldRejectImageWhenUrlFormatIsInvalid() throws Exception {

        AddProductImageRequest request =
                new AddProductImageRequest();

        request.setImageUrl(
                "ftp://example.com/image.jpg"
        );

        mockMvc.perform(
                        post("/api/products/product-1/images")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation Failed"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "imageUrl: Image URL must start with http:// or https://"
                        ))
                .andExpect(jsonPath("$.path")
                        .value("/api/products/product-1/images"));

        verifyNoInteractions(productService);
    }
}