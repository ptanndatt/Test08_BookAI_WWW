package com.iuh.bookstore.service;

import com.iuh.bookstore.entity.Book;
import com.iuh.bookstore.repository.BookRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AiBookService {

    private final ChatClient chatClient;
    private final BookRepository bookRepository;

    public AiBookService(ChatClient.Builder builder, BookRepository bookRepository) {
        this.chatClient = builder
                .defaultSystem("Bạn là trợ lý AI của website bán sách BookStore AI. Luôn trả lời bằng tiếng Việt, thân thiện, rõ ràng.")
                .build();
        this.bookRepository = bookRepository;
    }

    public String ask(String message) {
        List<Book> books = bookRepository.findAll();
        String bookData = buildBookData(books);

        String prompt = """
                Bạn là trợ lý AI của website bán sách BookStore AI.

                QUY TẮC TRẢ LỜI:
                - Luôn trả lời bằng tiếng Việt.
                - Nếu câu hỏi liên quan đến sách đang bán, giá sách, tác giả, số lượng tồn kho, mua sách, thanh toán:
                  chỉ được dùng dữ liệu trong DATABASE bên dưới.
                - Nếu không tìm thấy sách trong DATABASE, hãy nói: "Hiện website chưa có sách đó."
                - Nếu câu hỏi là kiến thức chung bên ngoài website, bạn được trả lời bằng kiến thức phổ thông của bạn.
                - Không được tự bịa giá, số lượng, tác giả hoặc tình trạng sách trong website.
                - Trả lời ngắn gọn, dễ hiểu, thân thiện.

                DATABASE SÁCH HIỆN CÓ:
                %s

                CÂU HỎI CỦA NGƯỜI DÙNG:
                %s
                """.formatted(bookData, message);

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            return answerFallback(message, books);
        }
    }

    private String buildBookData(List<Book> books) {
        if (books == null || books.isEmpty()) {
            return "Hiện chưa có sách nào trong database.";
        }

        StringBuilder sb = new StringBuilder();
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (Book book : books) {
            sb.append("- Tên sách: ").append(book.getTitle()).append("\n");
            sb.append("  Tác giả: ").append(book.getAuthor()).append("\n");
            sb.append("  Giá: ").append(currency.format(book.getPrice())).append("\n");
            sb.append("  Số lượng còn lại: ").append(book.getQuantity()).append("\n\n");
        }

        return sb.toString();
    }

    private String answerFallback(String message, List<Book> books) {
        if (books == null || books.isEmpty()) {
            return "Hiện database chưa có sách nào để tư vấn. Nếu muốn hỏi kiến thức chung, hãy cấu hình GEMINI_API_KEY để bật AI thật.";
        }

        String normalizedQuestion = normalize(message);
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        Optional<Book> matchedBook = books.stream()
                .filter(book -> normalizedQuestion.contains(normalize(book.getTitle())))
                .findFirst();

        if (matchedBook.isPresent()) {
            Book book = matchedBook.get();
            return "Sách \"" + book.getTitle() + "\" hiện có giá "
                    + currency.format(book.getPrice())
                    + ", tác giả " + book.getAuthor()
                    + ", còn lại " + book.getQuantity() + " cuốn.";
        }

        if (normalizedQuestion.contains("re nhat") || normalizedQuestion.contains("gia thap nhat")) {
            Book cheapest = books.stream().min(Comparator.comparing(Book::getPrice)).orElse(null);
            if (cheapest != null) {
                return "Sách rẻ nhất hiện tại là \"" + cheapest.getTitle() + "\" với giá "
                        + currency.format(cheapest.getPrice()) + ", tác giả " + cheapest.getAuthor() + ".";
            }
        }

        if (normalizedQuestion.contains("dat nhat") || normalizedQuestion.contains("gia cao nhat")) {
            Book mostExpensive = books.stream().max(Comparator.comparing(Book::getPrice)).orElse(null);
            if (mostExpensive != null) {
                return "Sách đắt nhất hiện tại là \"" + mostExpensive.getTitle() + "\" với giá "
                        + currency.format(mostExpensive.getPrice()) + ", tác giả " + mostExpensive.getAuthor() + ".";
            }
        }

        if (normalizedQuestion.contains("danh sach") || normalizedQuestion.contains("co sach nao") || normalizedQuestion.contains("tat ca sach")) {
            StringBuilder sb = new StringBuilder("Hiện website đang có các sách sau:\n");
            for (Book book : books) {
                sb.append("- ").append(book.getTitle())
                        .append(" - ").append(currency.format(book.getPrice()))
                        .append(" - còn ").append(book.getQuantity()).append(" cuốn\n");
            }
            return sb.toString();
        }

        if (isBookRelatedQuestion(normalizedQuestion)) {
            return "Hiện website chưa có sách đó hoặc chưa có dữ liệu phù hợp trong database.";
        }

        return "AI thật hiện chưa phản hồi được vì chưa cấu hình GEMINI_API_KEY hoặc Gemini đang lỗi. "
                + "Các câu hỏi liên quan đến sách trong database vẫn trả lời được, ví dụ: "
                + "\"Sách Chí Phèo giá bao nhiêu?\", \"Sách nào rẻ nhất?\", \"Danh sách sách hiện có?\".";
    }

    private boolean isBookRelatedQuestion(String question) {
        return question.contains("sach")
                || question.contains("gia")
                || question.contains("tac gia")
                || question.contains("so luong")
                || question.contains("con bao nhieu")
                || question.contains("ton kho")
                || question.contains("mua")
                || question.contains("thanh toan")
                || question.contains("don hang")
                || question.contains("hoc lap trinh")
                || question.contains("nen mua");
    }

    private String normalize(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        normalized = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        normalized = normalized.replace('đ', 'd').replace('Đ', 'D');
        return normalized;
    }
}
