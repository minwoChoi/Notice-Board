package com.example.demo.repository;


import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Category;
import com.example.demo.model.User;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryId(Long categoryId);
}