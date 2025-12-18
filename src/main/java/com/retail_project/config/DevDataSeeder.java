package com.retail_project.config;

import com.retail_project.category.Category;
import com.retail_project.category.CategoryRepository;
import com.retail_project.customer.Customer;
import com.retail_project.customer.CustomerRepository;
import com.retail_project.customer.Role;
import com.retail_project.product.ProductRepository;
import com.retail_project.product.ProductRequest;
import com.retail_project.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Override
    @Transactional
    public void run(String... args) {
        seedAdminIfMissing();
        seedCategoriesIfEmpty();
        seedProductsIfEmpty();
    }

    private void seedAdminIfMissing() {
        String adminEmail = "admin@retail.com";

        if (customerRepository.existsByEmail(adminEmail)) {
            return;
        }

        Customer admin = Customer.builder()
                .firstname("Admin")
                .lastname("Retail")
                .email(adminEmail)
                .password(passwordEncoder.encode("Admin123!"))
                .role(Role.ROLE_ADMIN)
                .address("Admin Address")
                .build();

        customerRepository.save(admin);

        System.out.println("✅ Seeded admin user: admin@retail.com / Admin123!");
    }

    private void seedCategoriesIfEmpty() {
        if (categoryRepository.count() > 0) return;

        List<Category> categories = List.of(
                Category.builder().name("Clothing").description("Clothing & apparel").build(),
                Category.builder().name("Shoes").description("Shoes & sneakers").build(),
                Category.builder().name("Accessories").description("Accessories").build()
        );

        categoryRepository.saveAll(categories);

        System.out.println("✅ Seeded categories: " + categories.size());
    }

    private void seedProductsIfEmpty() {
        if (productRepository.count() > 0) return;

        // Take first category as default (since we just seeded them)
        Integer categoryId = categoryRepository.findAll().get(0).getId();

        // ProductRequest order: name, description, price, categoryId, imageUrl
        productService.createProduct(new ProductRequest(
                "T-Shirt",
                "Soft cotton T-shirt",
                new BigDecimal("19.99"),
                categoryId,
                "https://placehold.co/600x400"
        ));

        productService.createProduct(new ProductRequest(
                "Sneakers",
                "Comfortable running sneakers",
                new BigDecimal("79.99"),
                categoryId,
                "https://placehold.co/600x400"
        ));

        productService.createProduct(new ProductRequest(
                "Backpack",
                "Laptop backpack for work and travel",
                new BigDecimal("49.99"),
                categoryId,
                "https://placehold.co/600x400"
        ));

        System.out.println("✅ Seeded demo products: 3");
    }
}
