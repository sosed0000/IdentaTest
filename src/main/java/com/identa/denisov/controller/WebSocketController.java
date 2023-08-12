package com.identa.denisov.controller;

import com.identa.denisov.model.Order;
import com.identa.denisov.model.OrderStatus;
import com.identa.denisov.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
@Controller
public class WebSocketController {

    private final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    private final OrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(OrderService orderService, SimpMessagingTemplate messagingTemplate) {
        this.orderService = orderService;
        this.messagingTemplate = messagingTemplate;
    }
    @MessageMapping("/newOrder")
    @SendTo("/topic/orders")
    public Order newOrder(String description) {
        Order order = orderService.createOrder(description);
        messagingTemplate.convertAndSend("/topic/orderStatusUpdate", order);
        return order;
    }

    @MessageMapping("/takeOrder/{orderId}")
    public void takeOrder(@DestinationVariable Long orderId) {
        Optional<Order> optionalOrder = orderService.getOrderById(orderId);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();

            if (order.getStatus() == OrderStatus.OPEN) {
                order.setStatus(OrderStatus.IN_PROGRESS);
                orderService.saveOrder(order);
                messagingTemplate.convertAndSend("/topic/orderStatusUpdate", order);
            } else {
                logger.info("Order is not open for processing.");
            }
        } else {
            logger.info("Order not found.");
        }
    }

    @MessageMapping("/completeOrder/{orderId}")
    public void completeOrder(@DestinationVariable Long orderId) {
        Optional<Order> optionalOrder = orderService.getOrderById(orderId);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();

            if (order.getStatus() == OrderStatus.IN_PROGRESS) {
                order.setStatus(OrderStatus.COMPLETED);
                orderService.saveOrder(order);
                messagingTemplate.convertAndSend("/topic/orderStatusUpdate", order);
            } else {
                logger.info("Order cannot be completed. It is not in progress.");
            }
        } else {
            logger.info("Order not found.");
        }
    }

}
