package com.bookvault.repository;

import com.bookvault.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Public books (not soft-deleted)
    @Query("SELECT b FROM Book b WHERE b.deletedAt IS NULL AND b.isPublic = true ORDER BY b.createdAt DESC")
    Page<Book> findAllPublicBooks(Pageable pageable);

    // All books for admin (not soft-deleted)
    @Query("SELECT b FROM Book b WHERE b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    Page<Book> findAllActiveBooks(Pageable pageable);

    // Find single active book
    @Query("SELECT b FROM Book b WHERE b.id = :id AND b.deletedAt IS NULL")
    Optional<Book> findActiveById(@Param("id") Long id);

    // Search public books
    @Query("SELECT b FROM Book b WHERE b.deletedAt IS NULL AND b.isPublic = true AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Book> searchPublicBooks(@Param("query") String query, Pageable pageable);

    // Search all books (admin)
    @Query("SELECT b FROM Book b WHERE b.deletedAt IS NULL AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Book> searchAllBooks(@Param("query") String query, Pageable pageable);

    // Genre filter
    @Query("SELECT b FROM Book b WHERE b.deletedAt IS NULL AND b.isPublic = true AND LOWER(b.genre) = LOWER(:genre)")
    Page<Book> findByGenre(@Param("genre") String genre, Pageable pageable);

    // Distinct genres
    @Query("SELECT DISTINCT b.genre FROM Book b WHERE b.deletedAt IS NULL AND b.genre IS NOT NULL ORDER BY b.genre")
    List<String> findDistinctGenres();

    // Increment view count
    @Modifying
    @Query("UPDATE Book b SET b.viewCount = b.viewCount + 1 WHERE b.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // Count active books
    @Query("SELECT COUNT(b) FROM Book b WHERE b.deletedAt IS NULL")
    long countActiveBooks();

    // Count public books
    @Query("SELECT COUNT(b) FROM Book b WHERE b.deletedAt IS NULL AND b.isPublic = true")
    long countPublicBooks();

    boolean existsByIsbn(String isbn);
}
