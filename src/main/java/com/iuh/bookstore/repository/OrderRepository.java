package com.iuh.bookstore.repository;

import com.iuh.bookstore.entity.Order;
import com.iuh.bookstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
}
