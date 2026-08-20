package dev.emanon.banking.customer.application.exception;

public class CustomerEmailAlreadyExistsException extends RuntimeException {

    public CustomerEmailAlreadyExistsException(String email) {
        super("Customer with email already exists: " + email);
    }
}