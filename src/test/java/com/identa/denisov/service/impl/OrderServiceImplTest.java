package com.identa.denisov.service.impl;

import com.identa.denisov.model.*;
import com.identa.denisov.repository.DishRepository;
import com.identa.denisov.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DishRepository dishRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }



    @Test
    void testCreateOrderWithDishes() {
        String description = "Test Order Description";
        SelectedDish selectedDish = new SelectedDish();
        selectedDish.setDishId(1L);
        selectedDish.setQuantity(2);
        List<SelectedDish> selectedDishes = List.of(selectedDish);

        Dish dish = new Dish();
        dish.setId(1L);
        dish.setName("Test Dish");
        dish.setPrice(10.0);

        when(dishRepository.findById(1L)).thenReturn(Optional.of(dish));
        when(dishRepository.save(any(Dish.class))).thenReturn(dish);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order createdOrder = orderService.createOrder(description, selectedDishes);

        assertNotNull(createdOrder);
        assertEquals(description, createdOrder.getDescription());
        assertEquals(1, createdOrder.getOrderedDishes().size());

        verify(dishRepository, times(1)).findById(1L);
        verify(dishRepository, times(1)).save(any(Dish.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testGetOrderById() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrderById(orderId);

        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getId());

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void testSaveOrder() {
        Order order = new Order();
        order.setId(1L);

        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.saveOrder(order);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(orderRepository, times(1)).save(order);
    }
    @Test
    void testAddDishToOrder() {
        Long orderId = 1L;
        Long dishId = 1L;
        int quantity = 5;

        Dish dish = new Dish();
        dish.setId(dishId);
        dish.setName("Test Dish");

        Order order = new Order();
        order.setId(orderId);
        List<OrderedDish> orderedDishes = new ArrayList<>();
        order.setOrderedDishes(orderedDishes);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(dishRepository.findById(dishId)).thenReturn(Optional.of(dish));

        orderService.addDishToOrder(orderId, dishId, quantity);

        Order updatedOrder = orderService.getOrderById(orderId).orElse(null);
        assertNotNull(updatedOrder);
        assertEquals(1, updatedOrder.getOrderedDishes().size());

        OrderedDish orderedDish = updatedOrder.getOrderedDishes().get(0);
        assertEquals(dishId, orderedDish.getDish().getId());
        assertEquals(quantity, orderedDish.getQuantity());

        verify(dishRepository, times(1)).findById(dishId);
        verify(orderRepository, times(2)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testGetAllDishes() {
        List<Dish> dishes = new ArrayList<>();
        dishes.add(new Dish());
        dishes.add(new Dish());

        when(dishRepository.findAll()).thenReturn(dishes);

        List<Dish> result = orderService.getAllDishes();

        assertEquals(2, result.size());

        verify(dishRepository, times(1)).findAll();
    }
}
