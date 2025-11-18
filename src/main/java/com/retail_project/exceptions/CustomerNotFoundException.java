package com.retail_project.exceptions;


public class CustomerNotFoundException extends EntityNotFoundException {
    public CustomerNotFoundException(Integer id) {
        super("Customer", id);
    }
}
