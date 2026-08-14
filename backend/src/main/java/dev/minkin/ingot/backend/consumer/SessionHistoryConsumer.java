package dev.minkin.ingot.backend.consumer;

import io.nats.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SessionHistoryConsumer {

    JetStreamManagement jsm;

    public SessionHistoryConsumer(Connection connection, JetStream jetStream) throws Exception {
        Dispatcher dispatcher = connection.createDispatcher();
        MessageHandler handler = (Message msg) -> {
            try {

                processEvent(msg);
                msg.ack();
            } catch (Exception e) {
                log.debug("Event failed");
                msg.ack();
            }
        };
        PushSubscribeOptions options = PushSubscribeOptions.builder()
                .stream("INGOT_EVENT_STREAM")
                .durable("history-projection")
                .build();

        jetStream.subscribe("events.workout_completed", dispatcher, handler, false, options);


    }

    private void processEvent(Message msg) {
        log.info("I have received an event {}", msg);
        // todo put in table
    }

    ;
}

