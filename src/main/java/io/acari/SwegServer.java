package io.acari;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class SwegServer {

    private HttpServer httpServer;

    public SwegServer(HttpServer httpServer, SwegHandler swegHandler) {
        this.httpServer = httpServer;
        this.httpServer.createContext("/", swegHandler);
        this.httpServer.setExecutor(Executors.newCachedThreadPool());
        this.httpServer.start();
    }


    public SwegServer(int serverPort) throws IOException {
        this(HttpServer.create(new InetSocketAddress(serverPort), 0), new SwegHandler());
    }
}
