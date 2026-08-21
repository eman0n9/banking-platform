package dev.emanon.banking.customer.infrastructure.outbox;

import dev.emanon.banking.customer.domain.Customer;
import dev.emanon.banking.customer.messaging.event.CustomerCreatedEvent;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import static dev.emanon.banking.customer.config.KafkaTopicConfig.CUSTOMER_EVENTS_TOPIC;

@Component
public class OutboxEventWriter {



    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper;

    public OutboxEventWriter(
            OutboxEventRepository outboxEventRepository,
            JsonMapper jsonMapper
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.jsonMapper = jsonMapper;
    }

    public void saveCustomerCreated(Customer customer) {
        CustomerCreatedEvent event =
                CustomerCreatedEvent.from(customer);

        String payload = serialize(event);

        OutboxEvent outboxEvent = OutboxEvent.pending(
                event.eventId(),
                "Customer",
                customer.getId(),
                event.eventType(),
                CUSTOMER_EVENTS_TOPIC,
                customer.getId().toString(),
                payload
        );

        outboxEventRepository.save(outboxEvent);
    }

    private String serialize(CustomerCreatedEvent event) {
        try {
            return jsonMapper.writeValueAsString(event);
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Failed to serialize CustomerCreatedEvent",
                    exception
            );
        }
    }
}