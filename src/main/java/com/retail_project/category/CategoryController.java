package com.retail_project.category;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@AllArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    //todo post create category
    @Operation(summary = "Create Category")
    @PostMapping
    public ResponseEntity<CategoryResponse >createCategory(@RequestBody @Valid CategoryRequest request){
        return ResponseEntity.ok(categoryService.createCategory(request));
    }


    //todo get all categories
    @Operation(summary = "Get all categories")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(){
        return ResponseEntity.ok(categoryService.getCategories());
    }

    //todo get category by Id
    @Operation(summary = "Get Category by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Integer id){
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }


    //todo update category by id
    @Operation(summary = "Update category by ID")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Integer id,@RequestBody CategoryRequest request){
        return ResponseEntity.ok(categoryService.updateCategory(id,request));
    }


    // todo delete category by id
    @Operation(summary = "Delete category by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id){
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    //todo search category

    //todo pagination category

}
