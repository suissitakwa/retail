package com.retail_project.product;

import com.retail_project.category.Category;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public Product toEntity(ProductRequest request, Category category) {
        return Product.builder().name(request.name())
                .description(request.description())
                .price(request.price())
                .category(category)
                .imageUrl(request.imageUrl()).build();

    }

    public ProductResponse toResponse(Product product) {
        Integer qty = (product.getInventory() == null) ? null : product.getInventory().getQuantity();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                qty
                ,product.getImageUrl()
        );
    }


}
