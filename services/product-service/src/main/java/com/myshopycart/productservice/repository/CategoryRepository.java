package com.myshopycart.productservice.repository;

import com.myshopycart.productservice.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {
}