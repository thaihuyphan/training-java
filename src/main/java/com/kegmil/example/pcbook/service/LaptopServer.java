package com.kegmil.example.pcbook.service;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LaptopServer {

  private static final Logger logger = Logger.getLogger(LaptopServer.class.getName());

  private final int port;
  private final Server server;

  public LaptopServer(int port, LaptopStore laptopStore) {
    this(ServerBuilder.forPort(port), port, laptopStore);
  }

  public LaptopServer(ServerBuilder serverBuilder, int port, LaptopStore laptopStore) {
    this.port = port;
    LaptopService laptopService = new LaptopService(laptopStore);
    server = serverBuilder.addService(laptopService).build();
  }

  public void start() throws IOException {
    server.start();
    logger.info("Server started on port: " + port);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.err.println("Shut down gRPC server due to JVM shut down");
      try {
        LaptopServer.this.stop();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.err.println("Server shut down");
    }));
  }

  public void stop() throws InterruptedException {
    if (server != null) {
      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }
  }

  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    InMemoryLaptopStore store = new InMemoryLaptopStore();
    LaptopServer server = new LaptopServer(6565, store);
    server.start();
    server.blockUntilShutdown();
  }
}
