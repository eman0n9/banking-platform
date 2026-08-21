package dev.emanon.banking.customer.api;

import dev.emanon.banking.customer.api.dto.*;
import dev.emanon.banking.customer.application.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        CustomerResponse response =
                customerService.createCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<CustomerResponse>> getCustomers(
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        PageResponse<CustomerResponse> response =
                customerService.getCustomers(pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomer(
            @PathVariable UUID customerId
    ) {
        CustomerResponse response =
                customerService.getCustomer(customerId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable UUID customerId,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        CustomerResponse response =
                customerService.updateCustomer(customerId, request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{customerId}/block")
    public ResponseEntity<CustomerResponse> blockCustomer(
            @PathVariable UUID customerId,
            @Valid @RequestBody ChangeCustomerStatusRequest request
    ) {
        return ResponseEntity.ok(
                customerService.blockCustomer(customerId, request)
        );
    }

    @PostMapping("/{customerId}/activate")
    public ResponseEntity<CustomerResponse> activateCustomer(
            @PathVariable UUID customerId,
            @Valid @RequestBody ChangeCustomerStatusRequest request
    ) {
        return ResponseEntity.ok(
                customerService.activateCustomer(customerId, request)
        );
    }

    @PostMapping("/{customerId}/close")
    public ResponseEntity<CustomerResponse> closeCustomer(
            @PathVariable UUID customerId,
            @Valid @RequestBody ChangeCustomerStatusRequest request
    ) {
        return ResponseEntity.ok(
                customerService.closeCustomer(customerId, request)
        );
    }
}