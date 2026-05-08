package com.iuh.bookstore.service;

import com.iuh.bookstore.entity.*;
import com.iuh.bookstore.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class OrderService {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public OrderService(BookRepository bookRepository, UserRepository userRepository, OrderRepository orderRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void buyBook(Long bookId, int quantity, String username) {
        if (quantity <= 0) throw new RuntimeException("Số lượng mua không hợp lệ");
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));
        if (book.getQuantity() < quantity) throw new RuntimeException("Sách không đủ số lượng tồn kho");
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        book.setQuantity(book.getQuantity() - quantity);
        bookRepository.save(book);

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(book.getPrice() * quantity);
        order.setStatus("Đã thanh toán");

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setBook(book);
        item.setQuantity(quantity);
        item.setPrice(book.getPrice());
        order.getItems().add(item);
        orderRepository.save(order);
    }
}
