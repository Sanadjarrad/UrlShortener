package com.shortener.urlshortener.helper;

import lombok.NoArgsConstructor;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Component
@NoArgsConstructor
public class UrlHelper {

    public static final String BASE_URL = "http://localhost:8080/UrlShortener";


    public static boolean isValidURL(String urlString) throws MalformedURLException {
        try {
            URL url = new URL(urlString);

            if (!url.getProtocol().equalsIgnoreCase("http") && !url.getProtocol().equalsIgnoreCase("https")) {
                throw new MalformedURLException("Invalid URL scheme: " + url.getProtocol());
            }

            String host = url.getHost();
            if (host == null || host.isEmpty() || host.startsWith(".") || host.endsWith(".")) {
                throw new MalformedURLException("Invalid host in URL: " + host);
            }

            if (host.contains("..")) {
                throw new MalformedURLException("Host contains consecutive dots: " + host);
            }

            int port = url.getPort() != -1 ? url.getPort() : url.getDefaultPort();
            if (port < 0 || port > 65535) {
                throw new MalformedURLException("Invalid port: " + port);
            }

            return true;
        } catch (MalformedURLException e) {
            throw new MalformedURLException("Invalid URL format provided: " + e.getMessage());
        }
    }

    public static String getBaseUrl(String longUrl) {
        try {
            URL url = new URL(longUrl);

            String protocol = url.getProtocol();
            String host = url.getHost();
            int port = url.getPort();

            if (port == -1) {
                return protocol + "://" + host;
            } else {
                return protocol + "://" + host + ":" + port;
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + longUrl);
        }
    }

    /*
    public static boolean isValidAndReachableURL(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();

            return responseCode >= 200 && responseCode < 300;
        } catch (URISyntaxException | IOException e) {
            return false;
        }
    }

    public static boolean isValidUrl(String urlString) {
        String[] schemes = {"http", "https", "ftp"};
        UrlValidator urlValidator = new UrlValidator(schemes);
        return urlValidator.isValid(urlString);
    }
     */
}
