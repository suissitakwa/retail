package com.retail_project.category;

import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public Category toEntity(CategoryRequest request){
        return Category.builder()
                        .name(request.name())
                        .description(request.description())
                        .build();

    }
    public CategoryResponse toResponse(Category category){
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }


}
