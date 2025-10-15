package com.retail_project.exceptions;

public class CategoryNotFoundException extends EntityNotFoundException{
    public CategoryNotFoundException(Integer id) {
        super("Category", id);
    }
}
