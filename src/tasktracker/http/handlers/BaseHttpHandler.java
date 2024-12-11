package tasktracker.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tasktracker.manager.Managers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements com.sun.net.httpserver.HttpHandler {
    protected final Gson gson = Managers.getGson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        try {
            switch (method) {
                case "GET":
                    processGet(exchange);
                    break;
                case "POST":
                    processPost(exchange);
                    break;
                case "DELETE":
                    processDelete(exchange);
                    break;
                default:
                    sendMethodNotAllowed(exchange, "Данный метод не предусмотрен");
            }
        } catch (IllegalArgumentException e) {
            // Если задача пересекается с другими — 406
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            sendText(exchange, "{\"error\": \"" + e.getMessage() + "\"}", 500);
        }
    }

    // Методы, которые по умолчанию возвращают 405, если не переопределены в наследнике.
    protected void processGet(HttpExchange exchange) throws IOException {
        sendMethodNotAllowed(exchange, "GET метод не предусмотрен");
    }

    protected void processPost(HttpExchange exchange) throws IOException {
        sendMethodNotAllowed(exchange, "POST метод не предусмотрен");
    }

    protected void processDelete(HttpExchange exchange) throws IOException {
        sendMethodNotAllowed(exchange, "DELETE метод не предусмотрен");
    }

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, "{\"error\": \"" + message + "\"}", 404);
    }

    protected void sendHasInteractions(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, "{\"error\": \"" + message + "\"}", 406);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, "{\"error\": \"" + message + "\"}", 405);
    }

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }
}
