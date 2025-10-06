package com.retail_project.customer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.message.LeaderAndIsrRequestData;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    //get customer by id
    public Optional<Customer> getCustomerById(int id){
        return customerRepository.findById(id);
    }
    // get all customers
    public List<Customer> getCustomers(){
        return customerRepository.findAll();
    }
    //create customer
    public Customer createCustomer(Customer customer ){
        return customerRepository.save(customer);
    }
    // update customer by id
    public Customer updateCustomer(int id,Customer customerDetails){
       Customer customer= customerRepository.findById(id).orElseThrow();
        customer.setFirstname(customerDetails.firstname);
        customer.setLastname(customerDetails.lastname);
        customer.setEmail(customerDetails.email);
        customer.setAddress(customer.address);
        return customer;
    }
    // delete customer by id
    public void deleteCustomer (int id){
         customerRepository.deleteById(id);
    }
}
