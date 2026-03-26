package com.bookvault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class BookDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Title is required")
        @Size(max = 500, message = "Title must be under 500 characters")
        private String title;

        @NotBlank(message = "Author is required")
        @Size(max = 300)
        private String author;

        @Size(max = 20)
        private String isbn;

        @NotBlank(message = "Version/Release is required")
        @Size(max = 50)
        private String versionRelease;

        @Size(max = 5000)
        private String description;

        @Size(max = 100)
        private String genre;

        @Size(max = 300)
        private String publisher;

        @Size(max = 50)
        private String language;

        private Integer pageCount;

        @Size(max = 1000)
        private String coverImageUrl;

        private boolean isPublic = true;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @Size(max = 500)
        private String title;

        @Size(max = 300)
        private String author;

        @Size(max = 20)
        private String isbn;

        @Size(max = 50)
        private String versionRelease;

        @Size(max = 5000)
        private String description;

        @Size(max = 100)
        private String genre;

        @Size(max = 300)
        private String publisher;

        @Size(max = 50)
        private String language;

        private Integer pageCount;

        @Size(max = 1000)
        private String coverImageUrl;

        private Boolean isPublic;
    }

    /** Public view — anyone can see this */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PublicResponse {
        private Long id;
        private String title;
        private String author;
        private String versionRelease;
        private String genre;
        private String coverImageUrl;
        private Long viewCount;
        private String createdAt;
        private String updatedAt;
    }

    /** Full view — authenticated users */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FullResponse {
        private Long id;
        private String title;
        private String author;
        private String isbn;
        private String versionRelease;
        private String description;
        private String genre;
        private String publisher;
        private String language;
        private Integer pageCount;
        private String coverImageUrl;
        private boolean hasPdf;
        private String pdfFileName;
        private Long pdfFileSize;
        private boolean isPublic;
        private Long viewCount;
        private String createdByUsername;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResponse {
        private java.util.List<?> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean last;
        private boolean first;
    }
}
