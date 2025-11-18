package com.retail_project.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail_project.customer.CustomerController;
import com.retail_project.customer.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.autoconfigure.data.jpa.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;

import java.util.List;


@WebMvcTest(controllers = CategoryController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class
                ,org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class
                //,org.springframework.boot.autoconfigure.data.jpa.JpaAuditingAutoConfiguration.class
} )
public class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void testCreateCategory() throws Exception {
        CategoryRequest request = new CategoryRequest("Books","Books category");
        CategoryResponse response = new CategoryResponse(2, "Books","Books category");

        when(categoryService.createCategory(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Books")));
    }
    @Test
    void testGetCategories() throws Exception {
        CategoryResponse category = new CategoryResponse(1, "Electronics","List of electronics");
        when(categoryService.getCategories()).thenReturn(List.of(category));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Electronics")));
    }
    @Test
    void testCreateCategory_EmptyName() throws Exception {
        CategoryRequest request = new CategoryRequest("","category description");

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


}
