package com.myshopycart.productservice.repository;

import com.myshopycart.productservice.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest(properties = {
        "spring.data.mongodb.uri=mongodb://host.docker.internal:27017/myshopycart_product_test"
})
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    private Product createProduct(
            String id,
            String name,
            String description,
            String categoryId
    ) {
        return new Product(
                id,
                name,
                description,
                BigDecimal.valueOf(1000),
                categoryId,
                "2",
                null,
                List.of(),
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    void shouldSaveAndFindProductById() {

        Product product = createProduct(
                "product-test-1",
                "Dell Laptop",
                "Business laptop",
                "cat-electronics"
        );

        Product savedProduct = productRepository.save(product);

        Product result = productRepository.findById(
                savedProduct.getId()
        ).orElseThrow();

        assertEquals("product-test-1", result.getId());
        assertEquals("Dell Laptop", result.getName());
        assertEquals("Business laptop", result.getDescription());
    }

    @Test
    void shouldFindProductsByCategoryId() {

        productRepository.save(createProduct(
                "product-test-1",
                "Dell Laptop",
                "Business laptop",
                "cat-electronics"
        ));

        productRepository.save(createProduct(
                "product-test-2",
                "HP Laptop",
                "Gaming laptop",
                "cat-electronics"
        ));

        productRepository.save(createProduct(
                "product-test-3",
                "Samsung Mobile",
                "Android phone",
                "cat-mobile"
        ));

        Page<Product> result = productRepository.findByCategoryId(
                "cat-electronics",
                PageRequest.of(0, 10)
        );

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void shouldSearchProductsByName() {

        productRepository.save(createProduct(
                "product-test-1",
                "Dell Laptop",
                "Business laptop",
                "cat-electronics"
        ));

        productRepository.save(createProduct(
                "product-test-2",
                "HP Laptop",
                "Gaming laptop",
                "cat-electronics"
        ));

        Page<Product> result = productRepository.searchProducts(
                "Dell",
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "Dell Laptop",
                result.getContent().get(0).getName()
        );
    }

    @Test
    void shouldSearchProductsByDescription() {

        productRepository.save(createProduct(
                "product-test-1",
                "Dell Laptop",
                "Powerful business laptop",
                "cat-electronics"
        ));

        productRepository.save(createProduct(
                "product-test-2",
                "HP Laptop",
                "Gaming computer",
                "cat-electronics"
        ));

        Page<Product> result = productRepository.searchProducts(
                "business",
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "Dell Laptop",
                result.getContent().get(0).getName()
        );
    }

    @Test
    void shouldSearchProductsCaseInsensitively() {

        productRepository.save(createProduct(
                "product-test-1",
                "Dell Laptop",
                "Business laptop",
                "cat-electronics"
        ));

        Page<Product> result = productRepository.searchProducts(
                "DELL",
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "Dell Laptop",
                result.getContent().get(0).getName()
        );
    }

    @Test
    void shouldPaginateSearchResults() {

        productRepository.save(createProduct(
                "product-test-1",
                "Dell Laptop",
                "Business laptop",
                "cat-electronics"
        ));

        productRepository.save(createProduct(
                "product-test-2",
                "Dell Monitor",
                "Business monitor",
                "cat-electronics"
        ));

        productRepository.save(createProduct(
                "product-test-3",
                "Dell Keyboard",
                "Business keyboard",
                "cat-electronics"
        ));

        Page<Product> result = productRepository.searchProducts(
                "Dell",
                PageRequest.of(0, 2)
        );

        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertTrue(result.hasNext());
    }
}