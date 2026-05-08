package com.iuh.bookstore.controller;

import com.iuh.bookstore.entity.Book;
import com.iuh.bookstore.repository.BookRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/admin/books")
public class AdminBookController {
    private final BookRepository bookRepository;

    public AdminBookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("books", bookRepository.findAll());
        return "admin/books";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("book", new Book());
        return "admin/book-form";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Book book, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        saveImage(book, imageFile);
        bookRepository.save(book);
        return "redirect:/admin/books";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));
        model.addAttribute("book", book);
        return "admin/book-form";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @ModelAttribute Book formBook, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));
        book.setTitle(formBook.getTitle());
        book.setAuthor(formBook.getAuthor());
        book.setPrice(formBook.getPrice());
        book.setQuantity(formBook.getQuantity());
        if (imageFile != null && !imageFile.isEmpty()) {
            saveImage(book, imageFile);
        }
        bookRepository.save(book);
        return "redirect:/admin/books";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        bookRepository.deleteById(id);
        return "redirect:/admin/books";
    }

    private void saveImage(Book book, MultipartFile imageFile) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String originalName = imageFile.getOriginalFilename();
                String extension = ".jpg";
                if (originalName != null && originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf("."));
                }
                String fileName = System.currentTimeMillis() + extension;
                Path uploadPath = Paths.get("uploads/book-covers");
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                book.setCoverImage("/book-covers/" + fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload ảnh: " + e.getMessage());
        }
    }
}
