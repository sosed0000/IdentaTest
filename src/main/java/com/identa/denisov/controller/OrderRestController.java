package com.identa.denisov.controller;

import com.identa.denisov.model.Dish;
import com.identa.denisov.model.Order;
import com.identa.denisov.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class OrderRestController {
    private final OrderService orderService;

    public OrderRestController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/getOrders")
    public List<Order> getOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/getDishes") // Добавляем новый эндпойнт для получения списка блюд
    public List<Dish> getDishes() {
        return orderService.getAllDishes(); // Вам нужно определить этот метод в вашем OrderService
    }
}
