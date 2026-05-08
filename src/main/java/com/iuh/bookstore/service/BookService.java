package com.iuh.bookstore.service;

import com.iuh.bookstore.entity.Book;
import com.iuh.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll() { return bookRepository.findAll(); }
    public List<Book> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return findAll();
        return bookRepository.findByTitleContainingIgnoreCase(keyword.trim());
    }
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách"));
    }
    public Book save(Book book) { return bookRepository.save(book); }
    public void delete(Long id) { bookRepository.deleteById(id); }
}
