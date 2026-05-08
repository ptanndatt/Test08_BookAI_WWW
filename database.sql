CREATE DATABASE IF NOT EXISTS bookstore_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE bookstore_ai;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL,
    cover_image VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_date DATETIME NOT NULL,
    total_amount DOUBLE NOT NULL,
    status VARCHAR(100) NOT NULL,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_items_book FOREIGN KEY (book_id) REFERENCES books(id)
);

INSERT IGNORE INTO users (id, username, password, role) VALUES
(1, 'admin', '$2a$10$QYct3MDnL9slN5KN0xVE0.KU0P7tBxdDPj5Bq2l.r96H/hbWliMTO', 'ADMIN'),
(2, 'user', '$2a$10$QYct3MDnL9slN5KN0xVE0.KU0P7tBxdDPj5Bq2l.r96H/hbWliMTO', 'USER');

INSERT IGNORE INTO books (id, title, author, price, quantity, cover_image) VALUES
(1, 'Chí Phèo', 'Nam Cao', 55000, 15, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=600'),
(2, 'Tắt Đèn', 'Ngô Tất Tố', 60000, 12, 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600'),
(3, 'Clean Code', 'Robert C. Martin', 250000, 8, 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=600'),
(4, 'Spring Boot Cơ Bản', 'Nguyễn Văn A', 180000, 20, 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=600');
