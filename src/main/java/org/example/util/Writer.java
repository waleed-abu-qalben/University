package org.example.util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Writer {
    public static final String TEXT_CONTENT = "text/plain";
    public static final String JSON_CONTENT = "application/json";

    public static void write(HttpServletResponse response,
                             int status, String contentType,
                             String msg) throws IOException {
        response.setStatus(status);
        response.setContentType(contentType);
        response.getWriter().write(msg);
    }
}
