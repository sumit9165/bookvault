package com.bookvault.controller;

import com.bookvault.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final BookService bookService;

    @GetMapping("/")
    public String home(Model model,
                       @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("featuredBooks",
                bookService.getPublicBooks(PageRequest.of(0, 8,
                        Sort.by("createdAt").descending())));
        model.addAttribute("genres", bookService.getAllGenres());
        model.addAttribute("user", userDetails);
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) return "redirect:/";
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) return "redirect:/";
        return "register";
    }

    @GetMapping("/books")
    public String booksPage(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "12") int size,
                            @RequestParam(required = false) String query,
                            @RequestParam(required = false) String genre,
                            @AuthenticationPrincipal UserDetails userDetails) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (query != null && !query.isBlank()) {
            model.addAttribute("books", bookService.searchPublicBooks(query, pageable));
            model.addAttribute("query", query);
        } else if (genre != null && !genre.isBlank()) {
            model.addAttribute("books", bookService.getBooksByGenre(genre, pageable));
            model.addAttribute("activeGenre", genre);
        } else {
            model.addAttribute("books", bookService.getPublicBooks(pageable));
        }
        model.addAttribute("genres", bookService.getAllGenres());
        model.addAttribute("user", userDetails);
        return "books";
    }

    @GetMapping("/books/{id}")
    public String bookDetail(@PathVariable Long id, Model model,
                             @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            model.addAttribute("book", bookService.getBookForReading(id, userDetails.getUsername()));
            model.addAttribute("canRead", true);
        } else {
            model.addAttribute("book", bookService.getPublicBookById(id));
            model.addAttribute("canRead", false);
        }
        model.addAttribute("user", userDetails);
        return "book-detail";
    }

    @GetMapping("/books/{id}/read")
    public String readBook(@PathVariable Long id, Model model,
                           @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        model.addAttribute("book", bookService.getBookForReading(id, userDetails.getUsername()));
        model.addAttribute("user", userDetails);
        return "book-reader";
    }

    @GetMapping("/dashboard")
    public String userDashboard(Model model,
                                @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        model.addAttribute("user", userDetails);
        model.addAttribute("recentBooks",
                bookService.getPublicBooks(PageRequest.of(0, 6, Sort.by("createdAt").descending())));
        return "dashboard";
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        model.addAttribute("user", userDetails);
        return "admin/dashboard";
    }

    @GetMapping("/admin/books")
    public String adminBooks(Model model,
                             @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        model.addAttribute("user", userDetails);
        return "admin/books";
    }

    @GetMapping("/admin/books/new")
    public String adminNewBook(Model model,
                               @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        model.addAttribute("user", userDetails);
        return "admin/book-form";
    }

    @GetMapping("/admin/books/{id}/edit")
    public String adminEditBook(@PathVariable Long id, Model model,
                                @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        model.addAttribute("book", bookService.getBookForReading(id, userDetails.getUsername()));
        model.addAttribute("user", userDetails);
        return "admin/book-form";
    }
}
