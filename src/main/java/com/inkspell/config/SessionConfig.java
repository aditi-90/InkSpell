package com.inkspell.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class SessionConfig implements ServletContextListener {
  public void contextInitialized(ServletContextEvent event) {
    event.getServletContext().getSessionCookieConfig().setAttribute("SameSite", "Lax");
  }
}
