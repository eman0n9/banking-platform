package dev.emanon.banking.customer.application;

import dev.emanon.banking.customer.api.dto.CreateCustomerRequest;
import dev.emanon.banking.customer.api.dto.CustomerResponse;
import dev.emanon.banking.customer.application.exception.CustomerEmailAlreadyExistsException;
import dev.emanon.banking.customer.application.exception.CustomerNotFoundException;
import dev.emanon.banking.customer.domain.Customer;
import dev.emanon.banking.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        String firstName = request.firstName().strip();
        String lastName = request.lastName().strip();

        String email = request.email()
                .strip()
                .toLowerCase(Locale.ROOT);

        String phone = normalizePhone(request.phone());

        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new CustomerEmailAlreadyExistsException(email);
        }

        Customer customer = new Customer(
                firstName,
                lastName,
                email,
                phone
        );

        Customer savedCustomer = customerRepository.save(customer);

        return CustomerResponse.from(savedCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFoundException(customerId)
                );

        return CustomerResponse.from(customer);
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }

        String normalizedPhone = phone.strip();

        if (normalizedPhone.isEmpty()) {
            return null;
        }

        return normalizedPhone;
    }
}