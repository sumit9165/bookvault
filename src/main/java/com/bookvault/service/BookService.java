package com.bookvault.service;

import com.bookvault.dto.BookDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {

    // Public
    BookDto.PageResponse getPublicBooks(Pageable pageable);
    BookDto.PageResponse searchPublicBooks(String query, Pageable pageable);
    BookDto.PublicResponse getPublicBookById(Long id);
    List<String> getAllGenres();
    BookDto.PageResponse getBooksByGenre(String genre, Pageable pageable);

    // Authenticated user
    BookDto.FullResponse getBookForReading(Long id, String username);

    // Admin operations
    BookDto.FullResponse createBook(BookDto.CreateRequest request, MultipartFile pdfFile, String username);
    BookDto.FullResponse updateBook(Long id, BookDto.UpdateRequest request, MultipartFile pdfFile, String username);
    void deleteBook(Long id, String username);
    BookDto.PageResponse getAllBooksAdmin(Pageable pageable);
    BookDto.PageResponse searchAllBooksAdmin(String query, Pageable pageable);
}
