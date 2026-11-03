
package com.myshopycart.productservice.service;

import com.myshopycart.productservice.entity.Product;
import com.myshopycart.productservice.exception.ForbiddenException;
import com.myshopycart.productservice.exception.ProductNotFoundException;
import com.myshopycart.productservice.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(Product product) {
        Authentication authentication = getCurrentAuthentication();

        if (!isAdmin(authentication)) {
            Long userId = getCurrentUserId(authentication);

            if (!String.valueOf(userId).equals(product.getDistributorId())) {
                throw new ForbiddenException(
                        "You are not authorized to create a product for this distributor");
            }
        }

        Instant now = Instant.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        return productRepository.save(product);
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Product> getMyProducts(Pageable pageable) {
        Authentication authentication = getCurrentAuthentication();

        if (isAdmin(authentication)) {
            return productRepository.findAll(pageable);
        }

        Long userId = getCurrentUserId(authentication);

        return productRepository.findByDistributorId(
                String.valueOf(userId),
                pageable);
    }

    public Page<Product> getProductsByCategory(
            String categoryId,
            Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    public Page<Product> searchProducts(
            String keyword,
            Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable);
    }

    @Cacheable(value = "products", key = "#id")
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found: " + id));
    }

    @CacheEvict(value = "products", key = "#id")
    public Product updateProduct(String id, Product product) {
        Product existingProduct = getProductById(id);
        Authentication authentication = getCurrentAuthentication();

        checkOwnership(existingProduct, authentication);

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setCategoryId(product.getCategoryId());
        existingProduct.setAttributes(product.getAttributes());
        existingProduct.setImageUrls(product.getImageUrls());
        existingProduct.setUpdatedAt(Instant.now());

        return productRepository.save(existingProduct);
    }

    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(String id) {
        Product existingProduct = getProductById(id);
        checkOwnership(existingProduct, getCurrentAuthentication());
        productRepository.deleteById(id);
    }

    @CacheEvict(value = "products", key = "#productId")
    public Product addProductImage(String productId, String imageUrl) {
        Product product = getProductById(productId);
        checkOwnership(product, getCurrentAuthentication());

        if (product.getImageUrls() == null) {
            product.setImageUrls(new ArrayList<>());
        }

        product.getImageUrls().add(imageUrl);
        product.setUpdatedAt(Instant.now());

        return productRepository.save(product);
    }

    @CacheEvict(value = "products", key = "#productId")
    public Product removeProductImage(String productId, String imageUrl) {
        Product product = getProductById(productId);
        checkOwnership(product, getCurrentAuthentication());

        if (product.getImageUrls() == null
                || !product.getImageUrls().contains(imageUrl)) {
            throw new RuntimeException("Image URL not found: " + imageUrl);
        }

        product.getImageUrls().remove(imageUrl);
        product.setUpdatedAt(Instant.now());

        return productRepository.save(product);
    }

    private Authentication getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getAuthorities() == null) {
            throw new ForbiddenException(
                    "Authenticated user information not available");
        }

        return authentication;
    }

    private Long getCurrentUserId(Authentication authentication) {
        Object details = authentication.getDetails();

        if (!(details instanceof Long userId)) {
            throw new ForbiddenException(
                    "Authenticated user ID is not available");
        }

        return userId;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private void checkOwnership(
            Product product,
            Authentication authentication) {

        if (isAdmin(authentication)) {
            return;
        }

        Long userId = getCurrentUserId(authentication);

        if (!String.valueOf(userId).equals(product.getDistributorId())) {
            throw new ForbiddenException(
                    "You are not authorized to modify this product");
        }
    }
}