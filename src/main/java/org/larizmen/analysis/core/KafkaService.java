package org.larizmen.analysis.core;

import org.larizmen.analysis.domain.*;

import io.smallrye.reactive.messaging.annotations.Blocking;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class KafkaService {

    Logger logger = LoggerFactory.getLogger(KafkaService.class);

    @Inject
    OrderService orderService;

    @Incoming("orders-in")
    @Blocking
    @Transactional
    public void orderIn(final PlaceOrderCommand placeOrderCommand) {
        logger.debug("PlaceOrderCommand received: {}", placeOrderCommand);
        orderService.onOrderIn(placeOrderCommand);
    }

    @Incoming("orders-out")
    @Blocking
    @Transactional
    public void orderUp(final ProcessTicket processTicket) {

        logger.debug("ProcessTicket received: {}", processTicket);
        orderService.onOrderUp(processTicket);
    }

}
