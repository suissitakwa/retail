package com.retail_project.customer;

import com.retail_project.exceptions.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.message.LeaderAndIsrRequestData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;

    //create customer
    public CustomerResponse createCustomer(CustomerRequest request){
        var customer =mapper.toEntity(request);
        return mapper.toResponse(customerRepository.save(customer));
    }

    //get customer by id
    public CustomerResponse getCustomerById(int id){
        return customerRepository.findById(id).map(mapper::toResponse).orElseThrow(()->new CustomerNotFoundException(id));
    }
    // get all customers
    public List<CustomerResponse> getCustomers(){
        return customerRepository.findAll().stream().map(mapper::toResponse).toList();
    }



    // update customer by id
    public CustomerResponse updateCustomer(int id,CustomerRequest request){
        Customer customer =customerRepository.findById(id).orElseThrow(()->new CustomerNotFoundException(id));
        customer.setFirstname(request.firstname());
        customer.setLastname(request.lastname());
        customer.setEmail(request.email());
        customer.setAddress(request.address());
        customerRepository.save(customer);
        return mapper.toResponse(customer);
    }

    private void mergeCustomer(Customer customer, CustomerRequest request) {
        if (StringUtils.isNotBlank(request.firstname())) {
            customer.setFirstname(request.firstname());
        }
        if (StringUtils.isNotBlank(request.lastname())) {
            customer.setLastname(request.lastname());
        }
        if (StringUtils.isNotBlank(request.email())) {
            customer.setEmail(request.email());
        }
        if (request.address() != null) {
            customer.setAddress(request.address());
        }
    }
    // delete customer by id
    public void deleteCustomer (int id){
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer not found");
        }
        customerRepository.deleteById(id);
    }

    //Pagination
    public Page<Customer> getAllCustomer(int page,int size) {
        Pageable pageable= PageRequest.of(page, size);
        return customerRepository.findAll(pageable);
    }
    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return mapper.toResponse(customer);
    }
}
