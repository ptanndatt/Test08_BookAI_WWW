package com.iuh.bookstore.service;

import com.iuh.bookstore.entity.Book;
import com.iuh.bookstore.repository.BookRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class AiService {
    private final ChatClient chatClient;
    private final BookRepository bookRepository;

    public AiService(ChatClient.Builder builder, BookRepository bookRepository) {
        this.chatClient = builder.build();
        this.bookRepository = bookRepository;
    }

    public String ask(String question) {
        List<Book> books = bookRepository.findAll();
        String bookData = buildBookData(books);
        String prompt = """
                Bạn là trợ lý AI của website bán sách BookStore AI.

                QUY TẮC TRẢ LỜI:
                - Luôn trả lời bằng tiếng Việt.
                - Nếu câu hỏi liên quan đến sách đang bán, giá sách, tác giả, số lượng tồn kho, mua sách, thanh toán: chỉ được dùng dữ liệu trong DATABASE bên dưới.
                - Nếu không tìm thấy sách trong DATABASE, hãy nói: "Hiện website chưa có sách đó."
                - Nếu câu hỏi là kiến thức chung bên ngoài website, bạn được trả lời bằng kiến thức phổ thông.
                - Không được tự bịa giá, số lượng, tác giả hoặc tình trạng sách.
                - Trả lời ngắn gọn, dễ hiểu, thân thiện.

                DỮ LIỆU SÁCH TRONG DATABASE:
                %s

                CÂU HỎI CỦA NGƯỜI DÙNG:
                %s
                """.formatted(bookData, question);
        try {
            return chatClient.prompt().user(prompt).call().content();
        } catch (Exception e) {
            return answerFallback(question, books);
        }
    }

    private String buildBookData(List<Book> books) {
        if (books == null || books.isEmpty()) return "Hiện chưa có sách nào trong database.";
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        StringBuilder sb = new StringBuilder();
        for (Book book : books) {
            sb.append("- Tên sách: ").append(book.getTitle()).append("\n");
            sb.append("  Tác giả: ").append(book.getAuthor()).append("\n");
            sb.append("  Giá: ").append(currency.format(book.getPrice())).append("\n");
            sb.append("  Số lượng còn lại: ").append(book.getQuantity()).append("\n\n");
        }
        return sb.toString();
    }

    private String answerFallback(String question, List<Book> books) {
        String lowerQuestion = removeVietnameseTone(question.toLowerCase());
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        if (lowerQuestion.contains("re nhat") || lowerQuestion.contains("gia thap nhat")) {
            return books.stream().min(Comparator.comparing(Book::getPrice))
                    .map(book -> "Sách rẻ nhất hiện tại là \"" + book.getTitle() + "\" giá " + currency.format(book.getPrice()) + ", còn " + book.getQuantity() + " cuốn.")
                    .orElse("Hiện website chưa có sách nào.");
        }

        for (Book book : books) {
            String bookTitle = removeVietnameseTone(book.getTitle().toLowerCase());
            if (lowerQuestion.contains(bookTitle)) {
                return "Sách \"" + book.getTitle() + "\" hiện có giá "
                        + currency.format(book.getPrice())
                        + ", tác giả " + book.getAuthor()
                        + ", còn lại " + book.getQuantity() + " cuốn.";
            }
        }

        if (isBookRelatedQuestion(lowerQuestion)) {
            return "Hiện website chưa có sách đó hoặc chưa có dữ liệu phù hợp trong database.";
        }

        return "Hiện Gemini chưa phản hồi được do chưa cấu hình API key hoặc key bị lỗi. Tuy nhiên bạn vẫn có thể hỏi các câu liên quan sách trong database, ví dụ: 'Sách Chí Phèo giá bao nhiêu?', 'Sách nào rẻ nhất?'.";
    }

    private boolean isBookRelatedQuestion(String question) {
        return question.contains("sach") || question.contains("gia") || question.contains("tac gia")
                || question.contains("con bao nhieu") || question.contains("so luong") || question.contains("mua")
                || question.contains("thanh toan") || question.contains("ton kho");
    }

    private String removeVietnameseTone(String str) {
        str = str.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        str = str.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        str = str.replaceAll("[ìíịỉĩ]", "i");
        str = str.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        str = str.replaceAll("[ùúụủũưừứựửữ]", "u");
        str = str.replaceAll("[ỳýỵỷỹ]", "y");
        str = str.replaceAll("đ", "d");
        return str;
    }
}
