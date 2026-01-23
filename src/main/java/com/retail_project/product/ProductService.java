package com.retail_project.product;

import com.retail_project.category.CategoryRepository;
import com.retail_project.exceptions.CategoryNotFoundException;
import com.retail_project.exceptions.ProductNotFoundException;
import com.retail_project.inventory.Inventory;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private ProductMapper mapper;

    //  Create should evict list cache (because list changes)
    @CacheEvict(cacheNames = "productsList", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        var product = mapper.toEntity(request, category);

        Inventory inv = new Inventory();
        inv.setQuantity(50);
        inv.setProduct(product);
        product.setInventory(inv);

        return mapper.toResponse(productRepository.save(product));
    }

    //  Cache the product list (short TTL via CacheConfig, e.g., 2 min)
    // Key is constant because this method currently has no paging/filtering
    @Cacheable(cacheNames = "productsList", key = "'all'", sync = true)
    public List<ProductResponse> getProducts(){
        return productRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    //  Cache product by id (TTL via CacheConfig, e.g., 15 min)
    @Cacheable(cacheNames = "productById", key = "#id", sync = true)
    public ProductResponse getProductById(Integer id){
        return productRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    //  Update: evict productById for this id + evict list cache
    @Caching(evict = {
            @CacheEvict(cacheNames = "productById", key = "#id"),
            @CacheEvict(cacheNames = "productsList", allEntries = true)
    })
    public ProductResponse updateProduct(Integer id, ProductRequest request){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(category);
        product.setImageUrl(request.imageUrl());

        return mapper.toResponse(productRepository.save(product));
    }

    // Delete: evict productById + list cache
    @Caching(evict = {
            @CacheEvict(cacheNames = "productById", key = "#id"),
            @CacheEvict(cacheNames = "productsList", allEntries = true)
    })
    public void deleteProduct(Integer id){
        productRepository.deleteById(id);
    }
}
