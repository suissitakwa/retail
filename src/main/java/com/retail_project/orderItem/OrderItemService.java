package com.retail_project.orderItem;


import com.retail_project.exceptions.ProductNotFoundException;
import com.retail_project.order.OrderRepository;
import com.retail_project.product.Product;
import com.retail_project.product.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderItemService {
    private final OrderItemRepository repository;
    private final ProductRepository productRepository;
    private  final OrderItemMapper mapper;
    //todo get by id
    public OrderItemResponse getById(Integer id){
        OrderItem item=repository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderItem not found: " + id));
        return mapper.toResponse(item);
    }

    //todo get by order id
    public List<OrderItemResponse> getByOrderId(Integer orderId){
            return repository.findByOrderId(orderId).stream().map(mapper::toResponse).toList();
    }

    //todo get all
    public List<OrderItemResponse> getAll(){
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    //todo update
    public OrderItemResponse update (Integer id,OrderItemRequest request){
        OrderItem item=repository.findById(id)
            .orElseThrow(() -> new RuntimeException("OrderItem not found: " + id));
        Product product=productRepository.findById(request.productId()).orElseThrow(()->new ProductNotFoundException(request.productId()));
        item.setProduct(product);
        item.setQuantity(request.quantity());
        item.setPrice(product.getPrice());
        return mapper.toResponse(repository.save(item));
    }


    //todo delete
    public void delete (Integer id){
        repository.deleteById(id);
    }

}
