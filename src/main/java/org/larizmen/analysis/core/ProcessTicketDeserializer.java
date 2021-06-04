package org.larizmen.analysis.core;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import org.larizmen.analysis.domain.ProcessTicket;

/**
 * Jackson deserializer for TicketUp value object
 */
public class ProcessTicketDeserializer extends ObjectMapperDeserializer<ProcessTicket> {

    public ProcessTicketDeserializer() {
        super(ProcessTicket.class);
    }
}
