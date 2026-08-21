package dev.emanon.banking.customer.api;

import dev.emanon.banking.customer.domain.CustomerStatus;
import dev.emanon.banking.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import dev.emanon.banking.customer.domain.Customer;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CustomerApiIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18.4");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void cleanDatabase() {
        customerRepository.deleteAll();
    }

    @Test
    void shouldCreateCustomer() throws Exception {
        String requestBody = """
                {
                  "firstName": "Dara",
                  "lastName": "Integration",
                  "email": "dara.integration@example.com",
                  "phone": "+420777111444"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.firstName").value("Dara"))
                .andExpect(jsonPath("$.lastName").value("Integration"))
                .andExpect(jsonPath("$.email")
                        .value("dara.integration@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.version").value(0));
    }

    @Test
    void shouldReturnBadRequestForInvalidCustomer() throws Exception {
        String requestBody = """
            {
              "firstName": "",
              "lastName": "",
              "email": "not-an-email",
              "phone": ""
            }
            """;

        mockMvc.perform(
                        post("/api/v1/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(400));

        assertEquals(0, customerRepository.count());
    }

    @Test
    void shouldReturnConflictForDuplicateEmailIgnoringCase() throws Exception {
        String firstRequest = """
            {
              "firstName": "Dara",
              "lastName": "First",
              "email": "duplicate.api@example.com",
              "phone": "+420777111501"
            }
            """;

        String secondRequest = """
            {
              "firstName": "Anna",
              "lastName": "Second",
              "email": "DUPLICATE.API@EXAMPLE.COM",
              "phone": "+420777111502"
            }
            """;

        mockMvc.perform(
                        post("/api/v1/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(firstRequest)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(secondRequest)
                )
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(409));

        assertEquals(1, customerRepository.count());
    }

    @Test
    void shouldReturnCustomerById() throws Exception {
        Customer customer = new Customer(
                "Dara",
                "GetTest",
                "get.api@example.com",
                "+420777111601"
        );

        Customer savedCustomer =
                customerRepository.saveAndFlush(customer);

        mockMvc.perform(
                        get(
                                "/api/v1/customers/{customerId}",
                                savedCustomer.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id")
                        .value(savedCustomer.getId().toString()))
                .andExpect(jsonPath("$.firstName").value("Dara"))
                .andExpect(jsonPath("$.lastName").value("GetTest"))
                .andExpect(jsonPath("$.email")
                        .value("get.api@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.version").value(0));
    }

    @Test
    void shouldReturnNotFoundForUnknownCustomerId() throws Exception {
        UUID unknownCustomerId = UUID.randomUUID();

        mockMvc.perform(
                get(
                        "/api/v1/customers/{customerId}",
                        unknownCustomerId
                )
        )
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value(
                        "/api/v1/customers/" + unknownCustomerId
                ));
    }

    @Test
    void shouldReturnCustomersPage() throws Exception {
        Customer firstCustomer = new Customer(
                "Dara",
                "First",
                "page.first@example.com",
                "+420777111701"
        );

        Customer secondCustomer = new Customer(
                "Dara",
                "Second",
                "page.second@example.com",
                "+420777111702"
        );

        Customer thirdCustomer = new Customer(
                "Dara",
                "Third",
                "page.third@example.com",
                "+420777111703"
        );

        customerRepository.saveAllAndFlush(
                java.util.List.of(
                        firstCustomer,
                        secondCustomer,
                        thirdCustomer
                )
        );

        mockMvc.perform(
                        get("/api/v1/customers")
                                .queryParam("page", "0")
                                .queryParam("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void shouldPartiallyUpdateCustomer() throws Exception {
        Customer customer = new Customer(
                "Dara",
                "Original",
                "patch.original@example.com",
                "+420777111801"
        );

        Customer savedCustomer =
                customerRepository.saveAndFlush(customer);

        String requestBody = """
            {
              "firstName": "Updated",
              "email": "patch.updated@example.com",
              "version": 0
            }
            """;

        mockMvc.perform(
                        patch(
                                "/api/v1/customers/{customerId}",
                                savedCustomer.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Original"))
                .andExpect(jsonPath("$.email")
                        .value("patch.updated@example.com"))
                .andExpect(jsonPath("$.phone")
                        .value("+420777111801"))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void shouldReturnConflictForStaleVersionDuringUpdate() throws Exception {
        Customer customer = new Customer(
                "Dara",
                "VersionConflict",
                "patch.version@example.com",
                "+420777111802"
        );

        Customer savedCustomer =
                customerRepository.saveAndFlush(customer);

        String requestBody = """
            {
              "firstName": "ShouldNotBeSaved",
              "version": 99
            }
            """;

        mockMvc.perform(
                        patch(
                                "/api/v1/customers/{customerId}",
                                savedCustomer.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        Customer unchangedCustomer = customerRepository
                .findById(savedCustomer.getId())
                .orElseThrow();

        assertEquals("Dara", unchangedCustomer.getFirstName());
        assertEquals(0, unchangedCustomer.getVersion());
    }

    @Test
    void shouldChangeCustomerStatusesAndRejectReopeningClosedCustomer()
            throws Exception {

        Customer customer = new Customer(
                "Dara",
                "StatusTest",
                "status.api@example.com",
                "+420777111901"
        );

        Customer savedCustomer =
                customerRepository.saveAndFlush(customer);

        UUID customerId = savedCustomer.getId();


        mockMvc.perform(
                        post(
                                "/api/v1/customers/{customerId}/block",
                                customerId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "version": 0
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.version").value(1));


        mockMvc.perform(
                        post(
                                "/api/v1/customers/{customerId}/activate",
                                customerId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "version": 1
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.version").value(2));


        mockMvc.perform(
                        post(
                                "/api/v1/customers/{customerId}/close",
                                customerId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "version": 2
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.version").value(3));


        mockMvc.perform(
                        post(
                                "/api/v1/customers/{customerId}/activate",
                                customerId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "version": 3
                                    }
                                    """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        Customer closedCustomer = customerRepository
                .findById(customerId)
                .orElseThrow();

        assertEquals(
                CustomerStatus.CLOSED,
                closedCustomer.getStatus()
        );

        assertEquals(3, closedCustomer.getVersion());
    }
}