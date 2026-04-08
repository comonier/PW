package com.comonier.plugin.utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/*
 * Utility class to send JSON payloads to Discord Webhooks.
 * Operates on a separate thread to avoid server lag (compatible with Folia).
 */
public class DiscordWebhook {

    public static void send(String urlString, String jsonPayload) {
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.addRequestProperty("Content-Type", "application/json");
                connection.addRequestProperty("User-Agent", "Java-DiscordWebhook");
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                try (OutputStream stream = connection.getOutputStream()) {
                    stream.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                    stream.flush();
                }
                connection.getInputStream().close();
                connection.disconnect();
            } catch (Exception ignored) {}
        }).start();
    }
}
