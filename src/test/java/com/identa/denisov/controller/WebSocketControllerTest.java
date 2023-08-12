package com.identa.denisov.controller;

import com.identa.denisov.dto.CreateOrderRequest;
import com.identa.denisov.dto.OrderDTO;
import com.identa.denisov.model.Dish;
import com.identa.denisov.model.Order;
import com.identa.denisov.model.OrderStatus;
import com.identa.denisov.model.SelectedDish;
import com.identa.denisov.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
public class WebSocketControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketController webSocketController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void newOrderTest() {
        Order order = new Order();
        order.setId(1L);
        order.setDescription("Test Order");
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.OPEN);

        when(orderService.saveOrder(any(Order.class))).thenReturn(order);

        OrderDTO orderDTO = webSocketController.newOrder(order);

        assertEquals(order.getId(), orderDTO.getId());
        assertEquals(order.getDescription(), orderDTO.getDescription());
        assertEquals(order.getCreatedAt(), orderDTO.getCreatedAt());
        assertEquals(order.getStatus(), orderDTO.getStatus());

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/orderStatusUpdate"), eq(order));
    }

    @Test
    void takeOrderTest() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.OPEN);

        when(orderService.getOrderById(orderId)).thenReturn(Optional.of(order));

        webSocketController.takeOrder(orderId);

        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());

        verify(orderService, times(1)).saveOrder(order);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/orderStatusUpdate"), any(OrderDTO.class));
    }

    @Test
    void completeOrderTest() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.IN_PROGRESS);

        when(orderService.getOrderById(orderId)).thenReturn(Optional.of(order));

        webSocketController.completeOrder(orderId);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());

        verify(orderService, times(1)).saveOrder(order);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/orderStatusUpdate"), eq(order));
    }

    @Test
    public void testAddDishToOrder() {
        Long orderId = 1L;
        Long dishId = 1L;
        int quantity = 2; // Just an example quantity

        Dish dish = new Dish();
        dish.setId(dishId);
        dish.setName("Test Dish");

        Order order = new Order();
        order.setId(orderId);

        when(orderService.getOrderById(orderId)).thenReturn(Optional.of(order));


        Order result = webSocketController.addDishToOrder(orderId, dishId, quantity);

        verify(orderService, times(1)).getOrderById(orderId);
        verify(orderService, times(1)).addDishToOrder(orderId, dishId, quantity); // Verify the correct arguments

        assertEquals(order, result);

        verifyNoMoreInteractions(messagingTemplate);
    }
    @Test
    void createOrderTest() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setDescription("Test Order");
        SelectedDish selectedDish = new SelectedDish();
        selectedDish.setDishId(1L);
        selectedDish.setQuantity(2);
        request.setSelectedDishes(Collections.singletonList(selectedDish));

        Order createdOrder = new Order();

        when(orderService.createOrder(eq("Test Order"), anyList())).thenReturn(createdOrder);

        ResponseEntity<OrderDTO> responseEntity = webSocketController.createOrder(request);

        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        verify(orderService, times(1)).createOrder(eq("Test Order"), anyList());
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/orderStatusUpdate"), eq(createdOrder));
    }
}