package com.bookvault.service.impl;

import com.bookvault.dto.BookDto;
import com.bookvault.entity.Book;
import com.bookvault.entity.User;
import com.bookvault.exception.ResourceNotFoundException;
import com.bookvault.repository.BookRepository;
import com.bookvault.repository.UserRepository;
import com.bookvault.service.BookService;
import com.bookvault.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ─── Public Methods ────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "publicBooks", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public BookDto.PageResponse getPublicBooks(Pageable pageable) {
        Page<Book> page = bookRepository.findAllPublicBooks(pageable);
        return toPageResponse(page, page.getContent().stream().map(this::toPublicResponse).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookDto.PageResponse searchPublicBooks(String query, Pageable pageable) {
        Page<Book> page = bookRepository.searchPublicBooks(query, pageable);
        return toPageResponse(page, page.getContent().stream().map(this::toPublicResponse).toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "books", key = "'public-' + #id")
    public BookDto.PublicResponse getPublicBookById(Long id) {
        Book book = bookRepository.findActiveById(id)
                .filter(Book::isPublic)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
        bookRepository.incrementViewCount(id);
        return toPublicResponse(book);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "genres")
    public List<String> getAllGenres() {
        return bookRepository.findDistinctGenres();
    }

    @Override
    @Transactional(readOnly = true)
    public BookDto.PageResponse getBooksByGenre(String genre, Pageable pageable) {
        Page<Book> page = bookRepository.findByGenre(genre, pageable);
        return toPageResponse(page, page.getContent().stream().map(this::toPublicResponse).toList());
    }

    // ─── Authenticated User Methods ────────────────────────────

    @Override
    @Transactional
    public BookDto.FullResponse getBookForReading(Long id, String username) {
        Book book = bookRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
        bookRepository.incrementViewCount(id);
        return toFullResponse(book);
    }

    // ─── Admin Methods ─────────────────────────────────────────

    @Override
    @Caching(evict = {
        @CacheEvict(value = "publicBooks", allEntries = true),
        @CacheEvict(value = "genres", allEntries = true)
    })
    public BookDto.FullResponse createBook(BookDto.CreateRequest request, MultipartFile pdfFile, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = Book.builder()
                .title(sanitize(request.getTitle()))
                .author(sanitize(request.getAuthor()))
                .isbn(request.getIsbn())
                .versionRelease(sanitize(request.getVersionRelease()))
                .description(request.getDescription() != null ? sanitize(request.getDescription()) : null)
                .genre(request.getGenre())
                .publisher(request.getPublisher())
                .language(request.getLanguage() != null ? request.getLanguage() : "English")
                .pageCount(request.getPageCount())
                .coverImageUrl(request.getCoverImageUrl())
                .isPublic(request.isPublic())
                .createdBy(user)
                .updatedBy(user)
                .build();

        if (pdfFile != null && !pdfFile.isEmpty()) {
            String filePath = fileStorageService.storePdf(pdfFile);
            book.setPdfFilePath(filePath);
            book.setPdfFileName(pdfFile.getOriginalFilename());
            book.setPdfFileSize(pdfFile.getSize());
        }

        book = bookRepository.save(book);
        log.info("Book created: '{}' by admin: {}", book.getTitle(), username);
        return toFullResponse(book);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "publicBooks", allEntries = true),
        @CacheEvict(value = "books", allEntries = true),
        @CacheEvict(value = "genres", allEntries = true)
    })
    public BookDto.FullResponse updateBook(Long id, BookDto.UpdateRequest request, MultipartFile pdfFile, String username) {
        Book book = bookRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getTitle() != null) book.setTitle(sanitize(request.getTitle()));
        if (request.getAuthor() != null) book.setAuthor(sanitize(request.getAuthor()));
        if (request.getIsbn() != null) book.setIsbn(request.getIsbn());
        if (request.getVersionRelease() != null) book.setVersionRelease(sanitize(request.getVersionRelease()));
        if (request.getDescription() != null) book.setDescription(sanitize(request.getDescription()));
        if (request.getGenre() != null) book.setGenre(request.getGenre());
        if (request.getPublisher() != null) book.setPublisher(request.getPublisher());
        if (request.getLanguage() != null) book.setLanguage(request.getLanguage());
        if (request.getPageCount() != null) book.setPageCount(request.getPageCount());
        if (request.getCoverImageUrl() != null) book.setCoverImageUrl(request.getCoverImageUrl());
        if (request.getIsPublic() != null) book.setPublic(request.getIsPublic());

        book.setUpdatedBy(user);

        if (pdfFile != null && !pdfFile.isEmpty()) {
            // Delete old PDF
            if (book.getPdfFilePath() != null) {
                fileStorageService.deleteFile(book.getPdfFilePath());
            }
            String filePath = fileStorageService.storePdf(pdfFile);
            book.setPdfFilePath(filePath);
            book.setPdfFileName(pdfFile.getOriginalFilename());
            book.setPdfFileSize(pdfFile.getSize());
        }

        book = bookRepository.save(book);
        log.info("Book updated: '{}' by admin: {}", book.getTitle(), username);
        return toFullResponse(book);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "publicBooks", allEntries = true),
        @CacheEvict(value = "books", allEntries = true),
        @CacheEvict(value = "genres", allEntries = true)
    })
    public void deleteBook(Long id, String username) {
        Book book = bookRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
        book.softDelete();
        bookRepository.save(book);
        log.info("Book soft-deleted: id={} by admin: {}", id, username);
    }

    @Override
    @Transactional(readOnly = true)
    public BookDto.PageResponse getAllBooksAdmin(Pageable pageable) {
        Page<Book> page = bookRepository.findAllActiveBooks(pageable);
        return toPageResponse(page, page.getContent().stream().map(this::toFullResponse).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookDto.PageResponse searchAllBooksAdmin(String query, Pageable pageable) {
        Page<Book> page = bookRepository.searchAllBooks(query, pageable);
        return toPageResponse(page, page.getContent().stream().map(this::toFullResponse).toList());
    }

    // ─── Mappers ───────────────────────────────────────────────

    private BookDto.PublicResponse toPublicResponse(Book book) {
        return BookDto.PublicResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .versionRelease(book.getVersionRelease())
                .genre(book.getGenre())
                .coverImageUrl(book.getCoverImageUrl())
                .viewCount(book.getViewCount())
                .createdAt(book.getCreatedAt() != null ? book.getCreatedAt().format(FORMATTER) : null)
                .updatedAt(book.getUpdatedAt() != null ? book.getUpdatedAt().format(FORMATTER) : null)
                .build();
    }

    private BookDto.FullResponse toFullResponse(Book book) {
        return BookDto.FullResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .versionRelease(book.getVersionRelease())
                .description(book.getDescription())
                .genre(book.getGenre())
                .publisher(book.getPublisher())
                .language(book.getLanguage())
                .pageCount(book.getPageCount())
                .coverImageUrl(book.getCoverImageUrl())
                .hasPdf(book.getPdfFilePath() != null)
                .pdfFileName(book.getPdfFileName())
                .pdfFileSize(book.getPdfFileSize())
                .isPublic(book.isPublic())
                .viewCount(book.getViewCount())
                .createdByUsername(book.getCreatedBy() != null ? book.getCreatedBy().getUsername() : "system")
                .createdAt(book.getCreatedAt() != null ? book.getCreatedAt().format(FORMATTER) : null)
                .updatedAt(book.getUpdatedAt() != null ? book.getUpdatedAt().format(FORMATTER) : null)
                .build();
    }

    private BookDto.PageResponse toPageResponse(Page<?> page, List<?> content) {
        return BookDto.PageResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    /** Basic XSS sanitization */
    private String sanitize(String input) {
        if (input == null) return null;
        return input.replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;")
                    .replace("/", "&#x2F;");
    }
}
