package com.kibikalo.read_aware.viewer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/read")
    public String readPage() {
        return "index";
    }

    @GetMapping("/books")
    public String bookListPage() {
        return "booklist";
    }

    @GetMapping("/upload")
    public String showUploadPage() {
        return "upload";
    }
}
