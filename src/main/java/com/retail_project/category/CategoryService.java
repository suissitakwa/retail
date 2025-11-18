package com.retail_project.category;

import com.retail_project.exceptions.CategoryNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

   //todo create category
    public CategoryResponse createCategory(CategoryRequest request){
        var category=mapper.toEntity(request);
        return mapper.toResponse(categoryRepository.save(category));
    }


    //todo get Category by Id
    public CategoryResponse getCategoryById(Integer id){


        return categoryRepository.findById(id).map(mapper::toResponse).orElseThrow(()->new CategoryNotFoundException(id));
    }


    //todo get all category
    public List<CategoryResponse> getCategories(){
        return categoryRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    //todo update category by id
    public CategoryResponse updateCategory(Integer id,CategoryRequest request){
        Category category=categoryRepository.findById(id).orElseThrow(()->new CategoryNotFoundException(id));
        category.setName(request.name());
        category.setDescription(request.description());
        categoryRepository.save(category);
        return mapper.toResponse(category);
    }


    //todo delete category by id
    public void deleteCategory (Integer id){

            categoryRepository.deleteById(id);
    }


        //todo search category by name

    //todo pagination category list
}
