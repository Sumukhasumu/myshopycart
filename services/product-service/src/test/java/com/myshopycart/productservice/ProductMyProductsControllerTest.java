
package com.myshopycart.productservice;

import com.myshopycart.productservice.entity.Product;
import com.myshopycart.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductMyProductsControllerTest {

    @Test
    void getMyProductsReturnsPagedProducts() throws Exception {
        ProductService productService = mock(ProductService.class);

        Product product = new Product();
        product.setName("Distributor Product");
        product.setDistributorId("2");

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> page =
                new PageImpl<>(List.of(product), pageable, 1);

        when(productService.getMyProducts(any(Pageable.class)))
                .thenReturn(page);

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new ProductCrudController(productService))
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver())
                .build();

        mockMvc.perform(get("/api/products/my-products")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name")
                        .value("Distributor Product"))
                .andExpect(jsonPath("$.content[0].distributorId")
                        .value("2"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(productService).getMyProducts(pageable);
    }
}