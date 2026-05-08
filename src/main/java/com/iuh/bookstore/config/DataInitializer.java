package com.iuh.bookstore.config;

import com.iuh.bookstore.entity.Book;
import com.iuh.bookstore.entity.User;
import com.iuh.bookstore.repository.BookRepository;
import com.iuh.bookstore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initData(UserRepository userRepository, BookRepository bookRepository, PasswordEncoder encoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(new User("admin", encoder.encode("123456"), "ADMIN"));
            }
            if (!userRepository.existsByUsername("user")) {
                userRepository.save(new User("user", encoder.encode("123456"), "USER"));
            }
            if (bookRepository.count() == 0) {
                bookRepository.save(new Book("Chí Phèo", "Nam Cao", 55000.0, 15, "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=600"));
                bookRepository.save(new Book("Tắt Đèn", "Ngô Tất Tố", 60000.0, 12, "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600"));
                bookRepository.save(new Book("Clean Code", "Robert C. Martin", 250000.0, 8, "https://images.unsplash.com/photo-1532012197267-da84d127e765?w=600"));
                bookRepository.save(new Book("Spring Boot Cơ Bản", "Nguyễn Văn A", 180000.0, 20, "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=600"));
            }
        };
    }
}
