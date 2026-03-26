package com.bookvault.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * XSS Prevention Filter
 *
 * Wraps every incoming HttpServletRequest so that all parameter values
 * (query string + form body) have dangerous HTML characters escaped before
 * they ever reach a controller.
 *
 * This is a defence-in-depth layer complementing:
 *  - Thymeleaf's auto-escaping (th:text)
 *  - BookServiceImpl.sanitize() on user-controlled book fields
 *  - Content-Security-Policy header set in SecurityConfig
 */
@Component
@Order(1)
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpReq) {
            chain.doFilter(new XssRequestWrapper(httpReq), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    // ── Wrapper ────────────────────────────────────────────────

    static class XssRequestWrapper extends HttpServletRequestWrapper {

        XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String[] getParameterValues(String parameter) {
            String[] values = super.getParameterValues(parameter);
            if (values == null) return null;
            String[] sanitized = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                sanitized[i] = cleanXss(values[i]);
            }
            return sanitized;
        }

        @Override
        public String getParameter(String parameter) {
            return cleanXss(super.getParameter(parameter));
        }

        @Override
        public String getHeader(String name) {
            return cleanXss(super.getHeader(name));
        }

        /**
         * Strips the most dangerous XSS vectors from a string value.
         * JSON request bodies are NOT processed here — those go through
         * Spring's Jackson deserializer and are sanitized at the service layer.
         */
        private String cleanXss(String value) {
            if (value == null || value.isBlank()) return value;

            // Remove null bytes
            value = value.replace("\0", "");

            // HTML-encode the five dangerous characters
            value = value.replace("&",  "&amp;")
                         .replace("<",  "&lt;")
                         .replace(">",  "&gt;")
                         .replace("\"", "&quot;")
                         .replace("'",  "&#x27;");

            // Strip javascript: and vbscript: URI schemes (case-insensitive)
            value = value.replaceAll("(?i)javascript\\s*:", "")
                         .replaceAll("(?i)vbscript\\s*:", "")
                         .replaceAll("(?i)on\\w+\\s*=", "");

            return value;
        }
    }
}
