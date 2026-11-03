package com.myshopycart.productservice.repository;

import com.myshopycart.productservice.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest(properties = {
        "spring.data.mongodb.uri=mongodb://host.docker.internal:27017/myshopycart_product_test"
})
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindCategoryById() {

        Category category = new Category(
                "cat-test-1",
                "Electronics",
                "Electronic products",
                null
        );

        Category savedCategory = categoryRepository.save(category);

        Optional<Category> result =
                categoryRepository.findById(savedCategory.getId());

        assertTrue(result.isPresent());
        assertEquals("cat-test-1", result.get().getId());
        assertEquals("Electronics", result.get().getName());
        assertEquals("Electronic products", result.get().getDescription());
    }

    @Test
    void shouldFindAllCategories() {

        Category category1 = new Category(
                "cat-test-1",
                "Electronics",
                "Electronic products",
                null
        );

        Category category2 = new Category(
                "cat-test-2",
                "Mobiles",
                "Mobile phones",
                "cat-test-1"
        );

        categoryRepository.save(category1);
        categoryRepository.save(category2);

        List<Category> categories = categoryRepository.findAll();

        assertEquals(2, categories.size());
    }

    @Test
    void shouldDeleteCategoryById() {

        Category category = new Category(
                "cat-test-1",
                "Electronics",
                "Electronic products",
                null
        );

        categoryRepository.save(category);

        categoryRepository.deleteById("cat-test-1");

        Optional<Category> result =
                categoryRepository.findById("cat-test-1");

        assertTrue(result.isEmpty());
    }
}