package dev.emanon.banking.customer.infrastructure.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(
        name = "outbox.publisher.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OutboxEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxEventPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxEventPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(
            fixedDelayString =
                    "${outbox.publisher.fixed-delay:1000}"
    )
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents =
                outboxEventRepository
                        .findTop100ByPublishedAtIsNullOrderByCreatedAtAsc();

        for (OutboxEvent event : pendingEvents) {
            boolean shouldContinue = publish(event);

            if (!shouldContinue) {
                return;
            }
        }
    }

    private boolean publish(OutboxEvent event) {
        try {
            kafkaTemplate.send(
                            event.getTopic(),
                            event.getEventKey(),
                            event.getPayload()
                    )
                    .get(10, TimeUnit.SECONDS);

            event.markPublished();

            log.info(
                    "Published outbox event: eventId={}, type={}, topic={}",
                    event.getEventId(),
                    event.getEventType(),
                    event.getTopic()
            );

            return true;

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            event.markFailed(exception.getMessage());

            log.warn(
                    "Outbox publisher thread interrupted: eventId={}",
                    event.getEventId(),
                    exception
            );

            return false;

        } catch (Exception exception) {
            event.markFailed(exception.getMessage());

            log.warn(
                    "Failed to publish outbox event: eventId={}",
                    event.getEventId(),
                    exception
            );

            return true;
        }
    }
}