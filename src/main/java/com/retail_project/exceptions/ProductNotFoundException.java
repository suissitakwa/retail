package com.retail_project.exceptions;

public class ProductNotFoundException extends EntityNotFoundException{
    public ProductNotFoundException(Integer id) {
        super("Product", id);
    }
}

