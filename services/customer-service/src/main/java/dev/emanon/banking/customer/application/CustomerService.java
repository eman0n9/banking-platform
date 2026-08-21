package dev.emanon.banking.customer.application;
import dev.emanon.banking.customer.api.dto.ChangeCustomerStatusRequest;
import dev.emanon.banking.customer.api.dto.*;
import dev.emanon.banking.customer.application.exception.CustomerEmailAlreadyExistsException;
import dev.emanon.banking.customer.application.exception.CustomerNotFoundException;
import dev.emanon.banking.customer.application.exception.CustomerVersionConflictException;
import dev.emanon.banking.customer.domain.Customer;
import dev.emanon.banking.customer.infrastructure.outbox.OutboxEventWriter;
import dev.emanon.banking.customer.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OutboxEventWriter outboxEventWriter;

    public CustomerService(
            CustomerRepository customerRepository,
            OutboxEventWriter outboxEventWriter
    ) {
        this.customerRepository = customerRepository;
        this.outboxEventWriter = outboxEventWriter;
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
        outboxEventWriter.saveCustomerCreated(savedCustomer);
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

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> getCustomers(
            Pageable pageable
    ) {
        Page<CustomerResponse> customers =
                customerRepository.findAll(pageable)
                        .map(CustomerResponse::from);

        return PageResponse.from(customers);
    }

    @Transactional
    public CustomerResponse updateCustomer(
            UUID customerId,
            UpdateCustomerRequest request
    ) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFoundException(customerId)
                );

        if (customer.getVersion() != request.version()) {
            throw new CustomerVersionConflictException(
                    customerId,
                    request.version(),
                    customer.getVersion()
            );
        }

        String firstName = request.firstName() == null
                ? customer.getFirstName()
                : request.firstName().strip();

        String lastName = request.lastName() == null
                ? customer.getLastName()
                : request.lastName().strip();

        String email = request.email() == null
                ? customer.getEmail()
                : request.email()
                .strip()
                .toLowerCase(Locale.ROOT);

        String phone = request.phone() == null
                ? customer.getPhone()
                : normalizePhone(request.phone());

        boolean emailChanged =
                !customer.getEmail().equalsIgnoreCase(email);

        if (emailChanged
                && customerRepository.existsByEmailIgnoreCase(email)) {

            throw new CustomerEmailAlreadyExistsException(email);
        }

        customer.updateContactInformation(
                firstName,
                lastName,
                email,
                phone
        );

        customerRepository.flush();

        return CustomerResponse.from(customer);
    }

    private Customer findCustomerForStatusChange(
            UUID customerId,
            long requestedVersion
    ) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        if (customer.getVersion() != requestedVersion) {
            throw new CustomerVersionConflictException(
                    customerId,
                    requestedVersion,
                    customer.getVersion()
            );
        }

        return customer;
    }

    @Transactional
    public CustomerResponse blockCustomer(
            UUID customerId,
            ChangeCustomerStatusRequest request
    ) {
        Customer customer = findCustomerForStatusChange(
                customerId,
                request.version()
        );

        customer.block();
        customerRepository.flush();

        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse activateCustomer(
            UUID customerId,
            ChangeCustomerStatusRequest request
    ) {
        Customer customer = findCustomerForStatusChange(
                customerId,
                request.version()
        );

        customer.activate();
        customerRepository.flush();

        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse closeCustomer(
            UUID customerId,
            ChangeCustomerStatusRequest request
    ) {
        Customer customer = findCustomerForStatusChange(
                customerId,
                request.version()
        );

        customer.close();
        customerRepository.flush();

        return CustomerResponse.from(customer);
    }
}