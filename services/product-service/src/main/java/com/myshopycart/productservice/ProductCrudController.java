
package com.myshopycart.productservice;

import com.myshopycart.productservice.dto.AddProductImageRequest;
import com.myshopycart.productservice.dto.CreateProductRequest;
import com.myshopycart.productservice.entity.Product;
import com.myshopycart.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductCrudController {

    private final ProductService productService;

    public ProductCrudController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategoryId(request.getCategoryId());
        product.setDistributorId(request.getDistributorId());
        product.setAttributes(request.getAttributes());
        product.setImageUrls(request.getImageUrls());

        Product createdProduct =
                productService.createProduct(product);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdProduct);
    }

    @GetMapping("/my-products")
    public ResponseEntity<Page<Product>> getMyProducts(
            Pageable pageable) {

        return ResponseEntity.ok(
                productService.getMyProducts(pageable));
    }

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            Pageable pageable) {

        return ResponseEntity.ok(
                productService.getAllProducts(pageable));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @PathVariable String categoryId,
            Pageable pageable) {

        return ResponseEntity.ok(
                productService.getProductsByCategory(
                        categoryId,
                        pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam String keyword,
            Pageable pageable) {

        return ResponseEntity.ok(
                productService.searchProducts(
                        keyword,
                        pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                productService.getProductById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable String id,
            @RequestBody Product product) {

        return ResponseEntity.ok(
                productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String id) {

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<Product> addProductImage(
            @PathVariable String id,
            @Valid @RequestBody AddProductImageRequest request) {

        Product updatedProduct =
                productService.addProductImage(
                        id,
                        request.getImageUrl());

        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}/images")
    public ResponseEntity<Product> removeProductImage(
            @PathVariable String id,
            @Valid @RequestBody AddProductImageRequest request) {

        Product updatedProduct =
                productService.removeProductImage(
                        id,
                        request.getImageUrl());

        return ResponseEntity.ok(updatedProduct);
    }
}