package com.retail_project.exceptions;


    public abstract class EntityNotFoundException extends RuntimeException {
        protected EntityNotFoundException(String entityName, Object id) {
            super(entityName + " with ID " + id + " not found");
        }
    }

