package com.identa.denisov.controller;

import com.identa.denisov.dto.CreateOrderRequest;
import com.identa.denisov.dto.OrderDTO;
import com.identa.denisov.model.Dish;
import com.identa.denisov.model.Order;
import com.identa.denisov.model.OrderStatus;
import com.identa.denisov.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
    public OrderDTO newOrder(Order order) {
        messagingTemplate.convertAndSend("/topic/orderStatusUpdate", order);
//возврат DTO
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setDescription(order.getDescription());
        orderDTO.setCreatedAt(order.getCreatedAt());
        orderDTO.setStatus(order.getStatus());

        return orderDTO;
    }

    @MessageMapping("/takeOrder/{orderId}")
    public void takeOrder(@DestinationVariable Long orderId) {
        Optional<Order> optionalOrder = orderService.getOrderById(orderId);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();

            if (order.getStatus() == OrderStatus.OPEN) {
                order.setStatus(OrderStatus.IN_PROGRESS);
                orderService.saveOrder(order);

                // Создание DTO и отправка
                OrderDTO orderDTO = new OrderDTO();
                orderDTO.setId(order.getId());
                orderDTO.setDescription(order.getDescription());
                orderDTO.setCreatedAt(order.getCreatedAt());
                orderDTO.setStatus(order.getStatus());

                messagingTemplate.convertAndSend("/topic/orderStatusUpdate", orderDTO);
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

    @MessageMapping("/addDishToOrder/{orderId}")
    @SendTo("/topic/orderStatusUpdate")
    public Order addDishToOrder(@DestinationVariable Long orderId, Dish dish) {
        orderService.addDishToOrder(orderId, dish);
        return orderService.getOrderById(orderId).orElse(null);
    }

    @PostMapping("/createOrder")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderRequest request) {
        String description = request.getDescription();
        List<Long> dishIds = request.getDishIds();

        // Создание заказа с описанием и блюдами
        Order order = orderService.createOrderWithDishes(description, dishIds);

        // Отправка обновленного статуса заказа через WebSocket
        messagingTemplate.convertAndSend("/topic/orderStatusUpdate", order);

        // Создание и возврат DTO
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setDescription(order.getDescription());
        orderDTO.setCreatedAt(order.getCreatedAt());
        orderDTO.setStatus(order.getStatus());

        return ResponseEntity.ok(orderDTO);
    }



}
