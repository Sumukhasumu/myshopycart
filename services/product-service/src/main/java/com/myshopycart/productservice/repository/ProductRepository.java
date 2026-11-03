
package com.myshopycart.productservice.repository;

import com.myshopycart.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ProductRepository extends MongoRepository<Product, String> {

    @Query("""
            {
              '$or': [
                { 'name': { '$regex': ?0, '$options': 'i' } },
                { 'description': { '$regex': ?0, '$options': 'i' } }
              ]
            }
            """)
    Page<Product> searchProducts(String keyword, Pageable pageable);

    Page<Product> findByCategoryId(String categoryId, Pageable pageable);

    Page<Product> findByDistributorId(
            String distributorId,
            Pageable pageable);
}