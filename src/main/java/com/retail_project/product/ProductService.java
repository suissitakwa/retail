package com.retail_project.product;

import com.retail_project.category.Category;
import com.retail_project.category.CategoryRepository;
import com.retail_project.exceptions.CategoryNotFoundException;
import com.retail_project.exceptions.ProductNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@AllArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private ProductMapper mapper;
    //todo create product
    public ProductResponse createProduct(ProductRequest request){
        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));
        var product=mapper.toEntity(request, category);
        return mapper.toResponse(productRepository.save(product));
    }

    //todo get all product
    public List<ProductResponse> getProducts(){
        return productRepository.findAll().stream().map(mapper::toResponse).toList();
    }
    //todo get product by id
    public ProductResponse getProductById(Integer id){
        return productRepository.findById(id)
                .map(mapper::toResponse).orElseThrow(()->new ProductNotFoundException(id));
    }
    //todo update product
    public ProductResponse updateProduct(Integer id,ProductRequest request){
        Product product=productRepository.findById(id).orElseThrow(()->new ProductNotFoundException(id));
        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(category);
        product.setImageUrl(request.imageUrl());
        return mapper.toResponse(productRepository.save(product));
    }
    //todo delete product
    public  void deleteProduct(Integer id){
        productRepository.deleteById(id);
    }
}
