
package com.myshopycart.productservice.service;

import com.myshopycart.productservice.entity.Product;
import com.myshopycart.productservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductMyProductsServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "distributor",
                        null,
                        List.of()
                );

        authentication.setDetails(2L);

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMyProductsReturnsOnlyCurrentDistributorsProducts() {
        Pageable pageable = PageRequest.of(0, 10);

        Product product = new Product();
        product.setName("Distributor Product");
        product.setDistributorId("2");

        Page<Product> expectedPage =
                new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findByDistributorId("2", pageable))
                .thenReturn(expectedPage);

        Page<Product> actualPage =
                productService.getMyProducts(pageable);

        assertSame(expectedPage, actualPage);
        assertEquals(1, actualPage.getTotalElements());
        assertEquals(
                "2",
                actualPage.getContent().get(0).getDistributorId()
        );

        verify(productRepository)
                .findByDistributorId("2", pageable);
    }
}