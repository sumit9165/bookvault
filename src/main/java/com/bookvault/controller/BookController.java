package com.bookvault.controller;

import com.bookvault.dto.ApiResponse;
import com.bookvault.dto.BookDto;
import com.bookvault.service.BookService;
import com.bookvault.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final FileStorageService fileStorageService;

    // ─── Public Endpoints ──────────────────────────────────────

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<BookDto.PageResponse>> getPublicBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), sort);
        return ResponseEntity.ok(ApiResponse.success(bookService.getPublicBooks(pageable)));
    }

    @GetMapping("/public/search")
    public ResponseEntity<ApiResponse<BookDto.PageResponse>> searchPublicBooks(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(ApiResponse.success(bookService.searchPublicBooks(query, pageable)));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponse<BookDto.PublicResponse>> getPublicBook(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getPublicBookById(id)));
    }

    @GetMapping("/genres")
    public ResponseEntity<ApiResponse<List<String>>> getGenres() {
        return ResponseEntity.ok(ApiResponse.success(bookService.getAllGenres()));
    }

    @GetMapping("/public/genre/{genre}")
    public ResponseEntity<ApiResponse<BookDto.PageResponse>> getByGenre(
            @PathVariable String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(ApiResponse.success(bookService.getBooksByGenre(genre, pageable)));
    }

    // ─── Authenticated User Endpoints ─────────────────────────

    @GetMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookDto.FullResponse>> getBookForReading(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success(
                bookService.getBookForReading(id, userDetails.getUsername())));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> streamPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        BookDto.FullResponse book = bookService.getBookForReading(id, userDetails.getUsername());

        if (!book.isHasPdf()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path pdfPath = fileStorageService.getPdfPath(book.getPdfFileName());
            Resource resource = new UrlResource(pdfPath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + book.getPdfFileName() + "\"")
                    .header("X-Frame-Options", "SAMEORIGIN")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── Admin Endpoints ───────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookDto.FullResponse>> createBook(
            @Valid @RequestPart("book") BookDto.CreateRequest request,
            @RequestPart(value = "pdf", required = false) MultipartFile pdfFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        BookDto.FullResponse created = bookService.createBook(request, pdfFile, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Book created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookDto.FullResponse>> updateBook(
            @PathVariable Long id,
            @Valid @RequestPart("book") BookDto.UpdateRequest request,
            @RequestPart(value = "pdf", required = false) MultipartFile pdfFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        BookDto.FullResponse updated = bookService.updateBook(id, request, pdfFile, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(updated, "Book updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        bookService.deleteBook(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.message("Book deleted successfully"));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookDto.PageResponse>> getAllBooksAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String query) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("createdAt").descending());
        if (query != null && !query.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(bookService.searchAllBooksAdmin(query, pageable)));
        }
        return ResponseEntity.ok(ApiResponse.success(bookService.getAllBooksAdmin(pageable)));
    }
}
