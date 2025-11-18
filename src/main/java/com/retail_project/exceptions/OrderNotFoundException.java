package com.retail_project.exceptions;

public class OrderNotFoundException extends EntityNotFoundException{
    public OrderNotFoundException(Integer id) {
        super("Order", id);
    }
}
