package com.inkspell.filter;

import com.inkspell.config.AppConfig;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

@WebFilter("/*")
public class SecurityFilter implements Filter {
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest r = (HttpServletRequest) request;
    HttpServletResponse s = (HttpServletResponse) response;
    r.setCharacterEncoding("UTF-8");
    s.setCharacterEncoding("UTF-8");
    s.setHeader("X-Content-Type-Options", "nosniff");
    s.setHeader("X-Frame-Options", "DENY");
    s.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    s.setHeader(
        "Content-Security-Policy",
        "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com;"
            + " font-src 'self' https://fonts.gstatic.com; img-src 'self' data: https:;"
            + " connect-src 'self' https://generativelanguage.googleapis.com; frame-ancestors 'none'; base-uri 'self'; form-action 'self'");

    boolean isHttps = r.isSecure() || "https".equalsIgnoreCase(r.getHeader("X-Forwarded-Proto"));
    if (isHttps) {
      s.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
    }

    // CORS support for Vercel deployment
    String origin = r.getHeader("Origin");
    if (origin != null && !origin.isBlank()) {
      String allowedFrontend = AppConfig.get("FRONTEND_URL", "");
      if (allowedFrontend.isBlank() || origin.equalsIgnoreCase(allowedFrontend) || origin.endsWith(".vercel.app")) {
        s.setHeader("Access-Control-Allow-Origin", origin);
        s.setHeader("Access-Control-Allow-Credentials", "true");
        s.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        s.setHeader("Access-Control-Allow-Headers", "Content-Type, X-CSRF-Token, Authorization, X-Requested-With");
      }
      if ("OPTIONS".equalsIgnoreCase(r.getMethod())) {
        s.setStatus(200);
        return;
      }
    }
    String path = r.getRequestURI().substring(r.getContextPath().length());
    if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")) {
      chain.doFilter(r, s);
      return;
    }
    s.setHeader("Cache-Control", "no-store");
    HttpSession session = r.getSession();
    if (session.getAttribute("csrf") == null) {
      byte[] b = new byte[32];
      new SecureRandom().nextBytes(b);
      session.setAttribute("csrf", Base64.getUrlEncoder().withoutPadding().encodeToString(b));
    }
    r.setAttribute("appName", AppConfig.APP_NAME);
    r.setAttribute("greeting", AppConfig.GREETING);
    r.setAttribute("year", java.time.Year.now().getValue());
    r.setAttribute("path", path);
    boolean privatePage =
        Set.of(
                    "/dashboard",
                    "/library",
                    "/bookmarks",
                    "/preferences",
                    "/profile",
                    "/assistant",
                    "/add-book",
                    "/logout")
                .contains(path)
            || path.startsWith("/admin")
            || path.startsWith("/api/");
    if (privatePage && session.getAttribute("user") == null) {
      if (path.startsWith("/api/")) {
        s.setStatus(401);
        s.setContentType("application/json");
        s.getWriter().write("{\"error\":\"Please enter the portal to continue.\"}");
      } else {
        if (r.getMethod().equals("GET"))
          session.setAttribute(
              "returnTo", path + (r.getQueryString() == null ? "" : "?" + r.getQueryString()));
        s.sendRedirect(r.getContextPath() + "/login");
      }
      return;
    }
    if (path.startsWith("/admin") && !"ADMIN".equals(session.getAttribute("role"))) {
      s.sendError(403);
      return;
    }
    if (!Set.of("GET", "HEAD", "OPTIONS").contains(r.getMethod())) {
      String token = r.getHeader("X-CSRF-Token");
      if (token == null) token = r.getParameter("csrf");
      if (!Objects.equals(session.getAttribute("csrf"), token)) {
        s.sendError(403, "This form expired. Reload the page and try again.");
        return;
      }
    }
    chain.doFilter(r, s);
  }
}
