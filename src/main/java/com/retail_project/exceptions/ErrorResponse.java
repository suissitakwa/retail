package com.retail_project.exceptions;

import java.util.Map;

public record ErrorResponse(
        Map<String,String> errors
) {
}