package com.bookvault.controller;

import com.bookvault.dto.ApiResponse;
import com.bookvault.repository.BookRepository;
import com.bookvault.repository.UserRepository;
import com.bookvault.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminStatsController
 *
 * Provides aggregate statistics for the admin dashboard.
 * All endpoints require the ADMIN role.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookService bookService;

    /**
     * GET /api/v1/admin/stats
     * Returns aggregate counts for the dashboard widgets.
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("totalBooks",   bookRepository.countActiveBooks());
        stats.put("publicBooks",  bookRepository.countPublicBooks());
        stats.put("totalUsers",   userRepository.count());
        stats.put("totalGenres",  bookRepository.findDistinctGenres().size());
        stats.put("generatedAt",  LocalDateTime.now().toString());

        return ApiResponse.success(stats);
    }

    /**
     * GET /api/v1/admin/genres
     * Returns all distinct genres with book counts.
     */
    @GetMapping("/genres")
    public ApiResponse<List<String>> getGenres() {
        return ApiResponse.success(bookRepository.findDistinctGenres());
    }
}
