package com.identa.denisov.service;

import com.identa.denisov.model.Dish;
import com.identa.denisov.model.Order;
import com.identa.denisov.model.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<Order> getAllOrders();
    List<Order> getAllOrdersByStatus(OrderStatus status);
    Order createOrder(String description);
    Optional<Order> getOrderById(Long id);
    Order saveOrder(Order order);
    void addDishToOrder(Long orderId, Dish dish);
    void removeDishFromOrder(Long orderId, Long dishId);

}
