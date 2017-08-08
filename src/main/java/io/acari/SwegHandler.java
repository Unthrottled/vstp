package io.acari;


import com.google.common.collect.Lists;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;

public class SwegHandler implements HttpHandler {
    public static final String CONTENT_LENGTH = "Content-Length";
    private static final String TARGET = "target";

    /**
     * Handles a given request and generates an appropriate response.
     * See {@link HttpExchange} for a description of the steps
     * involved in handling an httpExchange. Container invokes this method
     * when it receives an incoming request.
     *
     * @param httpExchange the httpExchange containing the request from the
     *                     client and used to send the response
     * @throws IOException when an I/O error happens during request
     *                     handling
     */
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            //I wish I could has Observable.
            Optional<HttpURLConnection> proxyConnection = getProxyConnection(httpExchange);
            OutputStream responseBody = httpExchange.getResponseBody();
            if (proxyConnection.isPresent()) {
                HttpURLConnection httpURLConnection = proxyConnection.get();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                String requestMethod = httpExchange.getRequestMethod();
                httpURLConnection.setRequestMethod(requestMethod);
                httpURLConnection.setRequestProperty("Cookie", "BUTT-TOKEN=POOPY");
                httpURLConnection.setRequestProperty("X-BUTT-TOKEN", "POOPY");
                byte[] response = getResponse(httpExchange);
                if (response.length > 1) {
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    outputStream.write(response);
                    outputStream.flush();
                }
                httpExchange.getRequestHeaders().entrySet()
                        .stream()
                        .flatMap(e -> e.getValue().stream().map(s -> new Pair<>(e.getKey(), s)))
                        .forEach(pair -> httpURLConnection.setRequestProperty(pair.getKey(), pair.getValue()));
                int responseCode = httpURLConnection.getResponseCode();
                
                httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
                httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                if (httpURLConnection.getHeaderField(CONTENT_LENGTH) != null) {
                    int size = Integer.parseInt(httpURLConnection.getHeaderField(CONTENT_LENGTH));
                    byte[] result = getBody(httpURLConnection.getInputStream(), size);
                    httpExchange.sendResponseHeaders(responseCode, result.length);
                    responseBody.write(result);
                } else {
                    InputStream stream = httpURLConnection.getInputStream();
                    httpExchange.sendResponseHeaders(responseCode, 0);
                    proxyChunked(stream, responseBody);
                }
                responseBody.flush();
                responseBody.close();
                httpExchange.close();
            } else {
                httpExchange.sendResponseHeaders(400, 0L);
                responseBody.write("Malformed or missing target query!".getBytes());
                httpExchange.close();
            }

        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                try {
                    httpExchange.sendResponseHeaders(404, "404".length());
                    httpExchange.getResponseBody().write("404".getBytes());
                    httpExchange.getResponseBody().flush();
                    httpExchange.close();
                } catch (IOException ignoreMe) {
                }
            } else {
                try {
                    httpExchange.sendResponseHeaders(418, e.getLocalizedMessage().length());
                    httpExchange.getResponseBody().write(e.getLocalizedMessage().getBytes());
                    httpExchange.getResponseBody().flush();
                    httpExchange.close();
                } catch (IOException ignoreMe) {
                }
            }
        }

    }

    private void proxyChunked(InputStream is, OutputStream os) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                os.write(line.getBytes());
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Optional<HttpURLConnection> getProxyConnection(HttpExchange httpExchange) throws IOException {
        URI requestURI = httpExchange.getRequestURI();
        String query = Optional.ofNullable(requestURI.getQuery()).orElse("");
        int contains = query.indexOf(TARGET);
        if(contains > -1){
            int beginIndex = contains + TARGET.length() + 1;
            String target = query.substring(beginIndex, query.indexOf('&', beginIndex));
            try {
                return Optional.of((HttpURLConnection) new URL(target).openConnection());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }
        return Optional.empty();
    }

    private byte[] getResponse(HttpExchange exchange) {
        if (exchange.getRequestHeaders().containsKey(CONTENT_LENGTH))
            return getBody(exchange.getRequestBody(), Integer.parseInt(exchange.getRequestHeaders().get(CONTENT_LENGTH).get(0)));
        return new byte[1];
    }

    private byte[] getBody(InputStream stream, int size) {
        byte[] iByte = new byte[1];
        byte[] buffer = new byte[size];
        int loc = 0;
        int read;
        try {
            while (true) {
                read = stream.read(iByte);
                if (read == 1) {
                    buffer[loc] = iByte[0];
                    loc++;
                }
                if (loc >= size)
                    break;
            }
            return buffer;
        } catch (IOException ignoreMe) {
            return new byte[0];
        }
    }
}
