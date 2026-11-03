package com.myshopycart.productservice;

import com.myshopycart.productservice.entity.Product;
import com.myshopycart.productservice.repository.ProductRepository;
import com.myshopycart.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);

        SecurityContextHolder.clearContext();

        setAuthenticatedUser(2L);
    }

    private void setAuthenticatedUser(Long userId) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "integrationuser",
                        null,
                        List.of()
                );

        authentication.setDetails(userId);

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }

    private Product createProduct(
            String id,
            String distributorId) {

        Product product = new Product();

        product.setId(id);
        product.setName("Dell Laptop");
        product.setDescription("Dell business laptop");
        product.setPrice(new BigDecimal("72999.99"));
        product.setCategoryId("category-1");
        product.setDistributorId(distributorId);
        product.setAttributes(
                Map.of("brand", "Dell")
        );
        product.setImageUrls(
                new java.util.ArrayList<>()
        );

        return product;
    }

    @Test
    void shouldCreateProductForAuthorizedDistributor() {

        Product product =
                createProduct("product-1", "2");

        when(productRepository.save(product))
                .thenReturn(product);

        Product result =
                productService.createProduct(product);

        assertNotNull(result);
        assertEquals("product-1", result.getId());
        assertEquals("2", result.getDistributorId());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(productRepository)
                .save(product);
    }

    @Test
    void shouldRejectProductCreationForAnotherDistributor() {

        Product product =
                createProduct("product-1", "999");

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> productService.createProduct(product)
                );

        assertEquals(
                "You are not authorized to create a product for this distributor",
                exception.getMessage()
        );

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void shouldGetProductById() {

        Product product =
                createProduct("product-1", "2");

        when(productRepository.findById("product-1"))
                .thenReturn(Optional.of(product));

        Product result =
                productService.getProductById("product-1");

        assertNotNull(result);
        assertEquals(
                "product-1",
                result.getId()
        );

        verify(productRepository)
                .findById("product-1");
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        when(productRepository.findById("missing"))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> productService.getProductById("missing")
                );

        assertEquals(
                "Product not found: missing",
                exception.getMessage()
        );
    }

    @Test
    void shouldGetAllProducts() {

        Product product =
                createProduct("product-1", "2");

        PageRequest pageable =
                PageRequest.of(0, 10);

        Page<Product> page =
                new PageImpl<>(
                        List.of(product),
                        pageable,
                        1
                );

        when(productRepository.findAll(pageable))
                .thenReturn(page);

        Page<Product> result =
                productService.getAllProducts(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "product-1",
                result.getContent().get(0).getId()
        );

        verify(productRepository)
                .findAll(pageable);
    }

    @Test
    void shouldSearchProducts() {

        Product product =
                createProduct("product-1", "2");

        PageRequest pageable =
                PageRequest.of(0, 10);

        Page<Product> page =
                new PageImpl<>(
                        List.of(product),
                        pageable,
                        1
                );

        when(productRepository.searchProducts(
                "Dell",
                pageable))
                .thenReturn(page);

        Page<Product> result =
                productService.searchProducts(
                        "Dell",
                        pageable
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "Dell Laptop",
                result.getContent().get(0).getName()
        );

        verify(productRepository)
                .searchProducts(
                        "Dell",
                        pageable
                );
    }

    @Test
    void shouldUpdateProductForOwner() {

        Product existingProduct =
                createProduct("product-1", "2");

        Product update =
                createProduct("product-1", "2");

        update.setName("Dell Latitude Updated");
        update.setPrice(
                new BigDecimal("79999.99")
        );

        when(productRepository.findById("product-1"))
                .thenReturn(
                        Optional.of(existingProduct)
                );

        when(productRepository.save(existingProduct))
                .thenReturn(existingProduct);

        Product result =
                productService.updateProduct(
                        "product-1",
                        update
                );

        assertEquals(
                "Dell Latitude Updated",
                result.getName()
        );

        assertEquals(
                new BigDecimal("79999.99"),
                result.getPrice()
        );

        verify(productRepository)
                .save(existingProduct);
    }

    @Test
    void shouldRejectUpdateForAnotherDistributor() {

        Product existingProduct =
                createProduct("product-1", "999");

        Product update =
                createProduct("product-1", "999");

        when(productRepository.findById("product-1"))
                .thenReturn(
                        Optional.of(existingProduct)
                );

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> productService.updateProduct(
                                "product-1",
                                update
                        )
                );

        assertEquals(
                "You are not authorized to modify this product",
                exception.getMessage()
        );

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void shouldDeleteProductForOwner() {

        Product product =
                createProduct("product-1", "2");

        when(productRepository.findById("product-1"))
                .thenReturn(Optional.of(product));

        productService.deleteProduct("product-1");

        verify(productRepository)
                .deleteById("product-1");
    }

    @Test
    void shouldRejectDeleteForAnotherDistributor() {

        Product product =
                createProduct("product-1", "999");

        when(productRepository.findById("product-1"))
                .thenReturn(Optional.of(product));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> productService.deleteProduct("product-1")
                );

        assertEquals(
                "You are not authorized to modify this product",
                exception.getMessage()
        );

        verify(productRepository, never())
                .deleteById(anyString());
    }

    @Test
    void shouldAddImageForOwner() {

        Product product =
                createProduct("product-1", "2");

        when(productRepository.findById("product-1"))
                .thenReturn(Optional.of(product));

        when(productRepository.save(product))
                .thenReturn(product);

        Product result =
                productService.addProductImage(
                        "product-1",
                        "https://example.com/laptop.jpg"
                );

        assertTrue(
                result.getImageUrls()
                        .contains(
                                "https://example.com/laptop.jpg"
                        )
        );

        verify(productRepository)
                .save(product);
    }

    @Test
    void shouldRejectImageAdditionForAnotherDistributor() {

        Product product =
                createProduct("product-1", "999");

        when(productRepository.findById("product-1"))
                .thenReturn(Optional.of(product));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> productService.addProductImage(
                                "product-1",
                                "https://example.com/hacked.jpg"
                        )
                );

        assertEquals(
                "You are not authorized to modify this product",
                exception.getMessage()
        );

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void shouldRemoveImageForOwner() {

        Product product =
                createProduct("product-1", "2");

        product.getImageUrls().add(
                "https://example.com/laptop.jpg"
        );

        when(productRepository.findById("product-1"))
                .thenReturn(Optional.of(product));

        when(productRepository.save(product))
                .thenReturn(product);

        Product result =
                productService.removeProductImage(
                        "product-1",
                        "https://example.com/laptop.jpg"
                );

        assertFalse(
                result.getImageUrls()
                        .contains(
                                "https://example.com/laptop.jpg"
                        )
        );

        verify(productRepository)
                .save(product);
    }

    @Test
    void shouldRejectImageRemovalForAnotherDistributor() {

        Product product =
                createProduct("product-1", "999");

        product.getImageUrls().add(
                "https://example.com/laptop.jpg"
        );

        when(productRepository.findById("product-1"))
                .thenReturn(Optional.of(product));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> productService.removeProductImage(
                                "product-1",
                                "https://example.com/laptop.jpg"
                        )
                );

        assertEquals(
                "You are not authorized to modify this product",
                exception.getMessage()
        );

        verify(productRepository, never())
                .save(any(Product.class));
    }
}