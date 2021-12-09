package com.kegmil.example.pcbook.service;

import com.kegmil.example.pcbook.pb.*;
import com.kegmil.example.pcbook.sample.Generator;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class LaptopServerTest {

  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  private LaptopStore store;
  private LaptopServer server;
  private ManagedChannel channel;

  @Before
  public void setUp() throws Exception {
    String serverName = InProcessServerBuilder.generateName();
    InProcessServerBuilder serverBuilder = InProcessServerBuilder.forName(serverName).directExecutor();

    store = new InMemoryLaptopStore();
    server = new LaptopServer(serverBuilder, 0, store);
    server.start();

    channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }

  @Test
  public void createLaptopWithAValidID() {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();
    CreateLaptopRequest request = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    CreateLaptopResponse response = stub.createLaptop(request);
    assertNotNull(response);
    assertEquals(laptop.getId(), response.getId());

    Laptop found = store.find(response.getId());
    assertNotNull(found);
  }

  @Test
  public void createLaptopWithAnEmptyID() {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop().toBuilder().setId("").build();
    CreateLaptopRequest request = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    CreateLaptopResponse response = stub.createLaptop(request);
    assertNotNull(response);
    assertFalse(response.getId().isEmpty());

    Laptop found = store.find(response.getId());
    assertNotNull(found);
  }

  @Test(expected = StatusRuntimeException.class)
  public void createLaptopWithAnInvalidID() {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop().toBuilder().setId("Invalid").build();
    CreateLaptopRequest request = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    CreateLaptopResponse response = stub.createLaptop(request);
  }

  @Test(expected = StatusRuntimeException.class)
  public void createLaptopWithAnAlreadyExistsID() throws Exception {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();
    store.save(laptop);
    CreateLaptopRequest request = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    CreateLaptopResponse response = stub.createLaptop(request);
  }

  @Test
  public void searchLaptopWithPriceGreaterThan1000() throws Exception {
    double expectedMoney = 1000;
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop().toBuilder()
            .setPriceUsd(expectedMoney)
            .build();
    store.save(laptop);

    Filter filter = Filter.newBuilder()
            .setMaxPriceUsd(expectedMoney + 1)
            .build();

    SearchLaptopRequest request = SearchLaptopRequest.newBuilder().setFilter(filter).build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    Iterator<SearchLaptopResponse> response = stub.searchLaptop(request);

    assertTrue(response.hasNext());

    while (response.hasNext()){
      SearchLaptopResponse rs = response.next();
      assertTrue(rs.getLaptop().getPriceUsd() < expectedMoney + 1);
    }
  }

  @Test
  public void searchLaptopWithPriceLessThan1000() throws Exception {
    double expectedMoney = 1000;
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop().toBuilder()
            .setPriceUsd(expectedMoney)
            .build();
    store.save(laptop);

    Filter filter = Filter.newBuilder()
            .setMaxPriceUsd(1000 - 1)
            .build();

    SearchLaptopRequest request = SearchLaptopRequest.newBuilder().setFilter(filter).build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    Iterator<SearchLaptopResponse> response = stub.searchLaptop(request);

    assertFalse(response.hasNext());
  }

  @Test(expected = StatusRuntimeException.class)
  public void updateLaptopWithAnEmptyID() {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop().toBuilder().setId("").build();
    UpdateLaptopRequest request = UpdateLaptopRequest.newBuilder().setLaptop(laptop).build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    UpdateLaptopResponse response = stub.updateLaptop(request);
  }

  @Test(expected = StatusRuntimeException.class)
  public void updateLaptopWithAnInvalidID() {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop().toBuilder().setId("Invalid").build();
    UpdateLaptopRequest request = UpdateLaptopRequest.newBuilder().setLaptop(laptop).build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    UpdateLaptopResponse response = stub.updateLaptop(request);
  }

  @Test(expected = StatusRuntimeException.class)
  public void updateLaptopWithANotExistsID() throws Exception {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();
    UpdateLaptopRequest request = UpdateLaptopRequest.newBuilder().setLaptop(laptop).build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    UpdateLaptopResponse response = stub.updateLaptop(request);
  }

  @Test
  public void updateLaptopWithAnExistsID() throws Exception {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();
    store.save(laptop);

    UpdateLaptopRequest request = UpdateLaptopRequest.newBuilder().setLaptop(laptop).build();
    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    UpdateLaptopResponse response = stub.updateLaptop(request);

    assertNotNull(response);
    assertEquals(laptop.getId(), response.getId());

    Laptop found = store.find(response.getId());
    assertNotNull(found);
  }

  @Test
  public void deleteLaptopWithAnExistsID() throws Exception {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();
    store.save(laptop);

    DeleteLaptopRequest request = DeleteLaptopRequest.newBuilder().setId(laptop.getId()).build();
    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    DeleteLaptopResponse response = stub.deleteLaptop(request);

    assertNotNull(response);
    assertTrue(response.getIsDeleted());
  }

  @Test(expected = StatusRuntimeException.class)
  public void deleteLaptopWithAnEmptyID() {
    DeleteLaptopRequest request = DeleteLaptopRequest.newBuilder().setId("").build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    DeleteLaptopResponse response = stub.deleteLaptop(request);
  }

  @Test(expected = StatusRuntimeException.class)
  public void deleteLaptopWithAnInvalidID() {
    DeleteLaptopRequest request = DeleteLaptopRequest.newBuilder().setId("Invalid").build();
    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    DeleteLaptopResponse response = stub.deleteLaptop(request);
  }

  @Test(expected = StatusRuntimeException.class)
  public void deleteLaptopWithANotExistsID() throws Exception {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();

    DeleteLaptopRequest request = DeleteLaptopRequest.newBuilder().setId(laptop.getId()).build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    DeleteLaptopResponse response = stub.deleteLaptop(request);
  }
}