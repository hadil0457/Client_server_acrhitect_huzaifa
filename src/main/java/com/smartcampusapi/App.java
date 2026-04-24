package com.smartcampusapi;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.net.URI;

// this is the main entry point for the whole api
// also doubles as the jax-rs Application subclass
@ApplicationPath("/api/v1")
public class App extends Application {

    private static final String SERVER_URL = "http://localhost:8080/api/v1/";

    public static void main(String[] args) {
        // register all classes in the package so jersey picks them up automatically
        ResourceConfig rc = new ResourceConfig().packages("com.smartcampusapi");

        System.out.println("starting server...");
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(SERVER_URL), rc);

        System.out.println("smart campus api is live -> " + SERVER_URL);
        System.out.println("ctrl+c to kill it");

        // keeps main thread alive otherwise the server just shuts down immediately
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("server shutting down");
            server.shutdownNow();
        }
    }
}

