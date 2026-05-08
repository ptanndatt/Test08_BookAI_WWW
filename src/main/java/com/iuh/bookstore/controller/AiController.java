package com.iuh.bookstore.controller;

import com.iuh.bookstore.service.AiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/ai")
    public String aiPage() {
        return "ai";
    }

    @PostMapping("/ai")
    public String ask(@RequestParam("question") String question, Model model) {
        model.addAttribute("question", question);
        model.addAttribute("answer", aiService.ask(question));
        return "ai";
    }
}
