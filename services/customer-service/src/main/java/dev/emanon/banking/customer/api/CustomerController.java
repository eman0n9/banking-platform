package dev.emanon.banking.customer.api;

import dev.emanon.banking.customer.api.dto.*;
import dev.emanon.banking.customer.application.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Customers",
        description = "Customer creation, retrieval, updating and status management"
)
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }
    @Operation(
            summary = "Create customer",
            description = "Creates a new banking customer with ACTIVE status"
    )
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
    @Operation(
            summary = "Get customers",
            description = "Returns a paginated list of customers"
    )
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

    @Operation(
            summary = "Get customer",
            description = "Returns a customer by UUID"
    )
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomer(
            @PathVariable UUID customerId
    ) {
        CustomerResponse response =
                customerService.getCustomer(customerId);

        return ResponseEntity.ok(response);
    }
    @Operation(
            summary = "Update customer",
            description = "Partially updates customer contact information using optimistic locking"
    )
    @PatchMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable UUID customerId,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        CustomerResponse response =
                customerService.updateCustomer(customerId, request);

        return ResponseEntity.ok(response);
    }
    @Operation(
            summary = "Block customer",
            description = "Changes customer status from ACTIVE to BLOCKED"
    )
    @PostMapping("/{customerId}/block")
    public ResponseEntity<CustomerResponse> blockCustomer(
            @PathVariable UUID customerId,
            @Valid @RequestBody ChangeCustomerStatusRequest request
    ) {
        return ResponseEntity.ok(
                customerService.blockCustomer(customerId, request)
        );
    }
    @Operation(
            summary = "Activate customer",
            description = "Changes customer status from BLOCKED to ACTIVE"
    )
    @PostMapping("/{customerId}/activate")
    public ResponseEntity<CustomerResponse> activateCustomer(
            @PathVariable UUID customerId,
            @Valid @RequestBody ChangeCustomerStatusRequest request
    ) {
        return ResponseEntity.ok(
                customerService.activateCustomer(customerId, request)
        );
    }
    @Operation(
            summary = "Close customer",
            description = "Permanently changes customer status to CLOSED"
    )
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