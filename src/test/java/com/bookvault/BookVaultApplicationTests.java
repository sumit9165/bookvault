package com.bookvault;

import com.bookvault.dto.AuthDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * BookVault Integration Tests
 *
 * These tests verify the core security and API contracts.
 *
 * To run:
 *   ./gradlew test
 *
 * Requirements: Running MySQL or MariaDB (or H2 in-memory if you switch datasource in test profile).
 * The easiest way is: docker compose up -d mysql redis  →  ./gradlew test
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class BookVaultApplicationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // ── Context Loads ──────────────────────────────────────────

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
        // If this test passes, the entire Spring context wired up correctly
    }

    // ── Public Endpoints ───────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/books/public returns 200 without authentication")
    void publicBooksEndpoint_returnsOk_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/books/public"))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/books/genres returns 200 without authentication")
    void genresEndpoint_returnsOk_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/books/genres"))
               .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /actuator/health returns 200 without authentication")
    void actuatorHealth_returnsOk_withoutAuth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
               .andExpect(status().isOk());
    }

    // ── Authentication Endpoints ───────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login with wrong credentials returns 401")
    void login_withWrongCredentials_returns401() throws Exception {
        AuthDto.LoginRequest request = new AuthDto.LoginRequest("wrong_user", "wrong_pass");

        mockMvc.perform(post("/api/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/register with invalid password returns 400")
    void register_withWeakPassword_returns400() throws Exception {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest("testuser99", "test@example.com", "weak");

        mockMvc.perform(post("/api/auth/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register with duplicate fields returns 409")
    void register_withDuplicateUsername_returns409() throws Exception {
        // First registration
        AuthDto.RegisterRequest req1 = new AuthDto.RegisterRequest(
                "uniqueUser1", "unique1@test.com", "Str0ng@Pass!");
        mockMvc.perform(post("/api/auth/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(req1)));

        // Same username again
        AuthDto.RegisterRequest req2 = new AuthDto.RegisterRequest(
                "uniqueUser1", "different@test.com", "Str0ng@Pass!");
        mockMvc.perform(post("/api/auth/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(req2)))
               .andExpect(status().isConflict());
    }

    // ── Protected Endpoints ────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/books/1/read without auth returns 401 or redirect")
    void readBook_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/books/1/read"))
               .andExpect(result -> {
                   int status = result.getResponse().getStatus();
                   // Either 401 Unauthorized or 302 redirect to login
                   assert status == 401 || status == 302
                       : "Expected 401 or 302, got " + status;
               });
    }

    @Test
    @DisplayName("POST /api/v1/books (admin create) without auth returns 401/403")
    void createBook_withoutAuth_returns401or403() throws Exception {
        mockMvc.perform(post("/api/v1/books")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{}"))
               .andExpect(result -> {
                   int s = result.getResponse().getStatus();
                   assert s == 401 || s == 403 : "Expected 401 or 403, got " + s;
               });
    }

    @Test
    @DisplayName("DELETE /api/v1/books/1 without auth returns 401/403")
    void deleteBook_withoutAuth_returns401or403() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1"))
               .andExpect(result -> {
                   int s = result.getResponse().getStatus();
                   assert s == 401 || s == 403 : "Expected 401 or 403, got " + s;
               });
    }

    // ── Security Headers ───────────────────────────────────────

    @Test
    @DisplayName("Response includes X-Content-Type-Options header")
    void response_hasXContentTypeOptionsHeader() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(header().exists("X-Content-Type-Options"));
    }

    @Test
    @DisplayName("Response includes X-Frame-Options header")
    void response_hasXFrameOptionsHeader() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(header().exists("X-Frame-Options"));
    }

    // ── Pages ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET / returns 200 homepage")
    void homepage_returnsOk() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /login returns 200 login page")
    void loginPage_returnsOk() throws Exception {
        mockMvc.perform(get("/login"))
               .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /register returns 200 register page")
    void registerPage_returnsOk() throws Exception {
        mockMvc.perform(get("/register"))
               .andExpect(status().isOk());
    }
}
