package org.larizmen.analysis.core;

//import io.debezium.outbox.quarkus.ExportedEvent;

import org.larizmen.analysis.domain.Order;
import org.larizmen.analysis.domain.OrderRepository;
import org.larizmen.analysis.domain.PlaceOrderCommand;
import org.larizmen.analysis.domain.OrderEventResult;
import org.larizmen.analysis.domain.OrderTicket;
import org.larizmen.analysis.domain.OrderUpdate;
import org.larizmen.analysis.domain.ProcessTicket;

import org.eclipse.microprofile.context.ThreadContext;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
// import javax.enterprise.event.Event;


@ApplicationScoped
public class OrderService {

    final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Inject
    ThreadContext threadContext;

    @Inject
    OrderRepository orderRepository;

//    @Inject
//    Event<ExportedEvent<?, ?>> event;

    @Channel("regularprocess-in")
    Emitter<OrderTicket> regularEmitter;

    @Channel("virusprocess-in")
    Emitter<OrderTicket> virusEmitter;

    @Channel("web-updates")
    Emitter<OrderUpdate> orderUpdateEmitter;

    @Transactional
    public void onOrderIn(final PlaceOrderCommand placeOrderCommand) {

        logger.debug("onOrderIn {}", placeOrderCommand);

        OrderEventResult orderEventResult = Order.process(placeOrderCommand);

        logger.debug("OrderEventResult returned and stored in DB: {}", orderEventResult);

        orderRepository.persist(orderEventResult.getOrder());

        orderEventResult.getOutboxEvents().forEach(exportedEvent -> {
            logger.debug("Firing event: {}", exportedEvent);
//            event.fire(exportedEvent);
        });

        if (orderEventResult.getRegularTickets().isPresent()) {
            orderEventResult.getRegularTickets().get().forEach(regularTicket -> {
                logger.debug("Sending Ticket to Regular Service: {}", regularTicket);
                regularEmitter.send(regularTicket);
            });
        }

        if (orderEventResult.getVirusTickets().isPresent()) {
            orderEventResult.getVirusTickets().get().forEach(virusTicket -> {
                logger.debug("Sending Ticket to Virus Service: {}", virusTicket);
                virusEmitter.send(virusTicket);
            });
        }

        orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
            logger.debug("Sending Update: {}", orderUpdate);
            orderUpdateEmitter.send(orderUpdate);
        });

    }

    @Transactional
    public void onOrderUp(final ProcessTicket processTicket) {

        logger.debug("Procesing Order: {}", processTicket);
        Order order = orderRepository.findById(processTicket.getOrderId());
        OrderEventResult orderEventResult = order.applyProcessTicket(processTicket);
        logger.debug("OrderEventResult returned: {}", orderEventResult);
        orderRepository.persist(orderEventResult.getOrder());
        orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
            logger.debug("Sending update: {}", orderUpdate);
            orderUpdateEmitter.send(orderUpdate);
        });
        orderEventResult.getOutboxEvents().forEach(exportedEvent -> {
 //           event.fire(exportedEvent);
        });
    }

    @Override
    public String toString() {
        return "OrderService{" +
                "threadContext=" + threadContext +
                ", orderRepository=" + orderRepository +
//                ", event=" + event +
                ", regularEmitter=" + regularEmitter +
                ", virusEmitter=" + virusEmitter +
                ", orderUpdateEmitter=" + orderUpdateEmitter +
                '}';
    }

}
