package com.myshopycart.productservice;

import com.myshopycart.productservice.entity.Product;
import com.myshopycart.productservice.repository.ProductRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "myshopycart.product.message=Integration Test",
        "myshopycart.product.service-name=product-service",
        "myshopycart.auth.jwt-secret=myshopycart-phase4-jwt-secret-key-change-this-later",
        "spring.cloud.config.enabled=false",
        "spring.data.mongodb.uri=mongodb://host.docker.internal:27017/myshopycart_product_test",
        "spring.data.redis.host=host.docker.internal",
        "spring.data.redis.database=15",
        "eureka.client.enabled=false"
})
@AutoConfigureMockMvc
class ProductIntegrationTest {

    private static final String JWT_SECRET =
            "myshopycart-phase4-jwt-secret-key-change-this-later";

    private static final Long USER_ID = 2L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    private String token;

    @BeforeEach
    void setUp() {

        // Clean MongoDB before every test
        productRepository.deleteAll();

        // Clean Redis products cache before every test
        Cache productsCache = cacheManager.getCache("products");

        if (productsCache != null) {
            productsCache.clear();
        }

        SecretKey key = Keys.hmacShaKeyFor(
                JWT_SECRET.getBytes(StandardCharsets.UTF_8)
        );

        token = Jwts.builder()
                .subject("integrationuser")
                .claim("userId", USER_ID)
                .claim("role", "DISTRIBUTOR")
                .issuedAt(new java.util.Date())
                .expiration(
                        new java.util.Date(
                                System.currentTimeMillis() + 3600000
                        )
                )
                .signWith(key)
                .compact();
    }

    @Test
    void shouldCreateProductAndPersistInMongoDB() throws Exception {

        String requestBody = """
                {
                    "name": "Integration Laptop",
                    "description": "Integration test laptop",
                    "price": 75000,
                    "categoryId": "cat-electronics",
                    "distributorId": "2",
                    "attributes": {
                        "brand": "Dell"
                    },
                    "imageUrls": []
                }
                """;

        mockMvc.perform(
                post("/api/products")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name")
                .value("Integration Laptop"))
        .andExpect(jsonPath("$.distributorId")
                .value("2"));

        assertEquals(1, productRepository.count());

        Product savedProduct =
                productRepository.findAll().get(0);

        assertEquals(
                "Integration Laptop",
                savedProduct.getName()
        );

        assertEquals(
                "2",
                savedProduct.getDistributorId()
        );
    }

    @Test
    void shouldGetProductById() throws Exception {

        Product product = createProduct(
                "integration-product-1",
                "Integration Phone",
                "Integration phone",
                "cat-mobile"
        );

        productRepository.save(product);

        mockMvc.perform(
                get("/api/products/integration-product-1")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id")
                .value("integration-product-1"))
        .andExpect(jsonPath("$.name")
                .value("Integration Phone"));
    }

    @Test
    void shouldCacheProductAfterFirstGet() throws Exception {

        Product product = createProduct(
                "cache-product-1",
                "Cache Test Product",
                "Product for cache test",
                "cat-test"
        );

        productRepository.save(product);

        Cache cache = cacheManager.getCache("products");

        assertTrue(cache != null);

        // Before GET, product should not be in Redis
        assertTrue(
                cache.get("cache-product-1") == null
        );

        // First GET:
        // Redis MISS -> MongoDB -> Redis
        mockMvc.perform(
                get("/api/products/cache-product-1")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id")
                .value("cache-product-1"))
        .andExpect(jsonPath("$.name")
                .value("Cache Test Product"));

        // Product should now be in Redis
        assertTrue(
                cache.get("cache-product-1") != null
        );
    }

    @Test
    void shouldReturnProductFromCacheOnSecondGet() throws Exception {

        Product product = createProduct(
                "cache-product-2",
                "Cache Hit Product",
                "Product for cache hit test",
                "cat-test"
        );

        productRepository.save(product);

        Cache cache = cacheManager.getCache("products");

        assertTrue(cache != null);

        // Initially cache should be empty
        assertTrue(
                cache.get("cache-product-2") == null
        );

        // First GET populates Redis
        mockMvc.perform(
                get("/api/products/cache-product-2")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id")
                .value("cache-product-2"))
        .andExpect(jsonPath("$.name")
                .value("Cache Hit Product"));

        // Confirm cache population
        assertTrue(
                cache.get("cache-product-2") != null
        );

        // Second GET should be served from cache
        mockMvc.perform(
                get("/api/products/cache-product-2")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id")
                .value("cache-product-2"))
        .andExpect(jsonPath("$.name")
                .value("Cache Hit Product"));
    }

    @Test
    void shouldEvictCacheAfterProductUpdate() throws Exception {

        Product product = createProduct(
                "cache-update-product",
                "Original Cache Product",
                "Original description",
                "cat-test"
        );

        productRepository.save(product);

        Cache cache = cacheManager.getCache("products");

        assertTrue(cache != null);

        // First GET:
        // Redis MISS -> MongoDB -> Redis
        mockMvc.perform(
                get("/api/products/cache-update-product")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name")
                .value("Original Cache Product"));

        // Verify product is cached
        assertTrue(
                cache.get("cache-update-product") != null
        );

        String requestBody = """
                {
                    "name": "Updated Cache Product",
                    "description": "Updated description",
                    "price": 90000,
                    "categoryId": "cat-test",
                    "distributorId": "2",
                    "attributes": {},
                    "imageUrls": []
                }
                """;

        // UPDATE:
        // MongoDB updated -> @CacheEvict -> Redis entry removed
        mockMvc.perform(
                put("/api/products/cache-update-product")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name")
                .value("Updated Cache Product"));

        // Verify update evicted the cached product
        assertTrue(
                cache.get("cache-update-product") == null
        );

        // GET again:
        // Redis MISS -> MongoDB -> Redis
        mockMvc.perform(
                get("/api/products/cache-update-product")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name")
                .value("Updated Cache Product"));

        // Cache should be populated again
        assertTrue(
                cache.get("cache-update-product") != null
        );
    }

    @Test
    void shouldSearchProducts() throws Exception {

        productRepository.save(createProduct(
                "integration-product-1",
                "Dell Laptop",
                "Business laptop",
                "cat-electronics"
        ));

        productRepository.save(createProduct(
                "integration-product-2",
                "HP Laptop",
                "Gaming laptop",
                "cat-electronics"
        ));

        mockMvc.perform(
                get("/api/products/search")
                        .param("keyword", "Dell")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$.content",
                hasSize(1)
        ))
        .andExpect(jsonPath(
                "$.content[0].name",
                is("Dell Laptop")
        ));
    }

    @Test
    void shouldRejectProductCreationForAnotherDistributor()
            throws Exception {

        String requestBody = """
                {
                    "name": "Unauthorized Product",
                    "description": "Should not be created",
                    "price": 5000,
                    "categoryId": "cat-test",
                    "distributorId": "999",
                    "attributes": {},
                    "imageUrls": []
                }
                """;

        mockMvc.perform(
                post("/api/products")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isForbidden());

        assertEquals(
                0,
                productRepository.count()
        );
    }

    @Test
    void shouldUpdateOwnedProduct() throws Exception {

        Product product = createProduct(
                "integration-product-1",
                "Old Name",
                "Old description",
                "cat-electronics"
        );

        productRepository.save(product);

        String requestBody = """
                {
                    "name": "Updated Name",
                    "description": "Updated description",
                    "price": 85000,
                    "categoryId": "cat-electronics",
                    "distributorId": "2",
                    "attributes": {},
                    "imageUrls": []
                }
                """;

        mockMvc.perform(
                put("/api/products/integration-product-1")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name")
                .value("Updated Name"));

        Product updated =
                productRepository.findById(
                        "integration-product-1"
                ).orElseThrow();

        assertEquals(
                "Updated Name",
                updated.getName()
        );
    }

    @Test
    void shouldDeleteOwnedProduct() throws Exception {

        productRepository.save(createProduct(
                "integration-product-1",
                "Delete Product",
                "Product to delete",
                "cat-test"
        ));

        mockMvc.perform(
                delete("/api/products/integration-product-1")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isNoContent());

        assertTrue(
                productRepository.findById(
                        "integration-product-1"
                ).isEmpty()
        );
    }

    @Test
    void shouldEvictCacheAfterProductDelete() throws Exception {

        Product product = createProduct(
                "cache-delete-product",
                "Cache Delete Product",
                "Product for delete cache test",
                "cat-test"
        );

        productRepository.save(product);

        Cache cache = cacheManager.getCache("products");

        assertTrue(cache != null);

        // First GET:
        // Redis MISS -> MongoDB -> Redis
        mockMvc.perform(
                get("/api/products/cache-delete-product")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id")
                .value("cache-delete-product"))
        .andExpect(jsonPath("$.name")
                .value("Cache Delete Product"));

        // Verify product is now cached
        assertTrue(
                 cache.get("cache-delete-product") != null
        );

        // DELETE:
        // MongoDB product deleted -> @CacheEvict -> Redis entry removed
        mockMvc.perform(
                delete("/api/products/cache-delete-product")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isNoContent());

        // Verify cache was evicted
        assertTrue(
                cache.get("cache-delete-product") == null
        );

        // Verify product is also removed from MongoDB
        assertTrue(
                productRepository.findById(
                        "cache-delete-product"
                ).isEmpty()
        );
    }

    @Test
    void shouldAddProductImage() throws Exception {

        productRepository.save(createProduct(
                "integration-product-1",
                "Image Product",
                "Product with image",
                "cat-test"
        ));

        String requestBody = """
                {
                    "imageUrl":
                    "https://example.com/integration-image.jpg"
                }
                """;

        mockMvc.perform(
                post("/api/products/integration-product-1/images")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$.imageUrls",
                hasSize(1)
        ))
        .andExpect(jsonPath(
                "$.imageUrls[0]",
                is("https://example.com/integration-image.jpg")
        ));
    }

    @Test
    void shouldRejectProductCreationWithoutJwt() throws Exception {

        mockMvc.perform(
                post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        )
        .andExpect(status().isForbidden());
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
}