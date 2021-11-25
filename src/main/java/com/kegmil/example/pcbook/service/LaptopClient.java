package com.kegmil.example.pcbook.service;

import com.kegmil.example.pcbook.pb.CreateLaptopRequest;
import com.kegmil.example.pcbook.pb.CreateLaptopResponse;
import com.kegmil.example.pcbook.pb.Filter;
import com.kegmil.example.pcbook.pb.LaptopServiceGrpc;
import com.kegmil.example.pcbook.pb.Memory;
import com.kegmil.example.pcbook.pb.SearchLaptopRequest;
import com.kegmil.example.pcbook.pb.SearchLaptopResponse;
import com.kegmil.example.pcbook.sample.Generator;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.kegmil.example.pcbook.pb.Laptop;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LaptopClient {
  private static final Logger logger = Logger.getLogger(LaptopClient.class.getName());

  private final ManagedChannel channel;
  private final LaptopServiceGrpc.LaptopServiceBlockingStub blockingStub;

  public LaptopClient(String host, int port) {
    channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

    blockingStub = LaptopServiceGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public void createLaptop(Laptop laptop) {
    CreateLaptopRequest request = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();
    CreateLaptopResponse response;

    try {
      response = blockingStub.createLaptop(request);
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.Code.ALREADY_EXISTS) {
        logger.info("laptop ID already exists");
        return;
      }
      logger.log(Level.SEVERE, "request failed: " + e.getMessage());
      return;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "request failed: " + e.getMessage());
      return;
    }

    logger.info("laptop created with ID: " + response.getId());
  }

  private void searchLaptop(Filter filter) {
    logger.info("Search started");

    SearchLaptopRequest request = SearchLaptopRequest.newBuilder().setFilter(filter).build();
    Iterator<SearchLaptopResponse> responseIterator = blockingStub.searchLaptop(request);

    while (responseIterator.hasNext()) {
      SearchLaptopResponse response = responseIterator.next();
      Laptop laptop = response.getLaptop();
      logger.info("- found: " + laptop.getId());
    }

    logger.info("Search completed");
  }

  public static void main(String[] args) throws InterruptedException {
    LaptopClient client = new LaptopClient("0.0.0.0", 6565);
    Generator generator = new Generator();

    try {
      for (int i = 0; i < 10; i++) {
        Laptop laptop = generator.newLaptop();
        client.createLaptop(laptop);
      }

      Memory minRam = Memory.newBuilder().setValue(8).setUnit(Memory.Unit.GIGABYTE).build();
      Filter filter = Filter.newBuilder()
          .setMaxPriceUsd(2000)
          .setMinCpuCores(4)
          .setMinCpuGhz(2.5)
          .setMinRam(minRam)
          .build();

      client.searchLaptop(filter);

    } finally {
      client.shutdown();
    }
  }
}
