package com.retail_project.order;

import com.retail_project.customer.Customer;
import com.retail_project.customer.CustomerRepository;
import com.retail_project.exceptions.CustomerNotFoundException;
import com.retail_project.exceptions.ProductNotFoundException;
import com.retail_project.inventory.Inventory;
import com.retail_project.inventory.InventoryRepository;
import com.retail_project.orderItem.OrderItem;
import com.retail_project.orderItem.OrderItemRequest;
import com.retail_project.product.Product;
import com.retail_project.product.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderFactory {

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final InventoryRepository inventoryRepository;

    public Order createFromRequest(OrderRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.customerId()));

        Order order = new Order();
        order.setReference(request.reference());
        order.setCustomer(customer);
        order.setPaymentMethod(PaymentMethod.valueOf(request.paymentMethod()));
        order.setCreatedDate(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ProductNotFoundException(itemReq.productId()));

            Inventory inventory = inventoryRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new RuntimeException("No inventory for product: " + product.getId()));

            if (inventory.getQuantity() < itemReq.quantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Decrement inventory
            inventory.setQuantity(inventory.getQuantity() - itemReq.quantity());
            inventory.setLastUpdated(LocalDateTime.now());
            inventoryRepository.save(inventory);

            // Create order item
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemReq.quantity());
            item.setPrice(product.getPrice());
            item.setOrder(order);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            items.add(item);
        }

        order.setTotalAmount(total);
        order.setOrderItems(items);

        return order;
    }

}

