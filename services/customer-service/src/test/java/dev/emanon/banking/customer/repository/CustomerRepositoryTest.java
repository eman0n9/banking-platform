package dev.emanon.banking.customer.repository;

import dev.emanon.banking.customer.domain.Customer;
import dev.emanon.banking.customer.domain.CustomerStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class CustomerRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:18.4");

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndFindCustomerByEmailIgnoringCase() {

        Customer customer = new Customer(
                "Dara",
                "Example",
                "dara.repository@example.com",
                "+420777111222"
        );


        customerRepository.saveAndFlush(customer);

        Optional<Customer> result =
                customerRepository.findByEmailIgnoreCase(
                        "DARA.REPOSITORY@EXAMPLE.COM"
                );


        assertTrue(result.isPresent());
        assertEquals(customer.getId(), result.get().getId());
        assertEquals(
                "dara.repository@example.com",
                result.get().getEmail()
        );
    }

    @Test
    void shouldRejectDuplicateEmailIgnoringCase() {
        Customer firstCustomer = new Customer(
                "Dara",
                "First",
                "duplicate.repository@example.com",
                "+420777111001"
        );

        Customer secondCustomer = new Customer(
                "Anna",
                "Second",
                "DUPLICATE.REPOSITORY@EXAMPLE.COM",
                "+420777111002"
        );

        customerRepository.saveAndFlush(firstCustomer);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> customerRepository.saveAndFlush(secondCustomer)
        );
    }

    @Test
    void shouldIncrementVersionWhenCustomerChanges() {
        Customer customer = new Customer(
                "Dara",
                "Version",
                "version.repository@example.com",
                "+420777111003"
        );

        Customer savedCustomer =
                customerRepository.saveAndFlush(customer);

        long versionBeforeUpdate = savedCustomer.getVersion();

        savedCustomer.block();
        customerRepository.flush();

        entityManager.clear();

        Customer updatedCustomer = customerRepository
                .findById(savedCustomer.getId())
                .orElseThrow();

        assertEquals(
                versionBeforeUpdate + 1,
                updatedCustomer.getVersion()
        );

        assertEquals(
                CustomerStatus.BLOCKED,
                updatedCustomer.getStatus()
        );
    }
}