package com.assessment.demo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.logging.Logger;

@WebServlet(name = "PingServlet", urlPatterns = {"/api/ping"})
public class PingServlet extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(PingServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String name = req.getParameter("name");
    if (name == null || name.isBlank()) name = "world";

    String requestId = UUID.randomUUID().toString();
    String pod = System.getenv().getOrDefault("HOSTNAME", "unknown");

    LOG.info(String.format(
        "requestId=%s event=ping name=%s pod=%s ts=%s remote=%s ua=%s",
        requestId,
        name,
        pod,
        Instant.now().toString(),
        req.getRemoteAddr(),
        req.getHeader("User-Agent")
    ));

    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().write("{\"ok\":true,\"name\":\"" + escapeJson(name) + "\",\"requestId\":\"" + requestId + "\"}");
  }

  private static String escapeJson(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}

