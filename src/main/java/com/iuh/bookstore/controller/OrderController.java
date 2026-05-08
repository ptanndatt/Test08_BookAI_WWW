package com.iuh.bookstore.controller;

import com.iuh.bookstore.entity.User;
import com.iuh.bookstore.repository.OrderRepository;
import com.iuh.bookstore.repository.UserRepository;
import com.iuh.bookstore.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, OrderRepository orderRepository, UserRepository userRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/buy/{bookId}")
    public String buy(@PathVariable Long bookId, @RequestParam(defaultValue = "1") int quantity, Authentication authentication) {
        orderService.buyBook(bookId, quantity, authentication.getName());
        return "redirect:/orders/my";
    }

    @GetMapping("/my")
    public String myOrders(Authentication authentication, Model model) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("orders", orderRepository.findByUserOrderByOrderDateDesc(user));
        return "orders";
    }
}
