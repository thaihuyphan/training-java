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
    assertNotNull(response);
  }

  @Test(expected = StatusRuntimeException.class)
  public void createLaptopWithAnAlreadyExistsID() throws Exception {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();
    store.save(laptop);
    CreateLaptopRequest request = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    CreateLaptopResponse response = stub.createLaptop(request);
    assertNotNull(response);
  }

  @Test
  public void searchLaptopWithAValidFilter() {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();

    Filter filter = Filter.newBuilder()
            .setMinRam(laptop.getRam())
            .build();

    SearchLaptopRequest request = SearchLaptopRequest.newBuilder().setFilter(filter).build();
    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    Iterator<SearchLaptopResponse> responseIterator = stub.searchLaptop(request);

    while (responseIterator.hasNext()) {
      SearchLaptopResponse response = responseIterator.next();
      assertNotNull(response);

      Laptop other = response.getLaptop();
      assertEquals(laptop.getId(), other.getId());
    }
  }

  @Test
  public void searchLaptopWithAnEmptyFilter() {
    Filter filter = Filter.newBuilder()
            .build();

    SearchLaptopRequest request = SearchLaptopRequest.newBuilder().setFilter(filter).build();
    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    Iterator<SearchLaptopResponse> responseIterator = stub.searchLaptop(request);

    //
    while (responseIterator.hasNext()) {
      SearchLaptopResponse response = responseIterator.next();
      assertNotNull(response);

      Laptop other = response.getLaptop();
      assertFalse(other.getId().isEmpty());
    }
  }

  @Test(expected = StatusRuntimeException.class)
  public void deleteLaptopWithAValidID() {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();
    DeleteLaptopRequest request = DeleteLaptopRequest.newBuilder().setLaptop(laptop).build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    stub.deleteLaptop(request);
    assertNull(store.find(laptop.getId()));
  }

  @Test(expected = StatusRuntimeException.class)
  public void deleteLaptopWithNonExistLaptop() {
    Laptop laptop = Laptop.newBuilder()
            .setBrand("Non-exist")
            .build();
    DeleteLaptopRequest request = DeleteLaptopRequest.newBuilder().setLaptop(laptop).build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    stub.deleteLaptop(request);
    //assert??
  }

  @Test
  public void updateLaptopWithAValidLaptop() {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();

    UpdateLaptopRequest request = UpdateLaptopRequest.newBuilder().setLaptop(laptop).build();
    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);

    stub.createLaptop(CreateLaptopRequest.newBuilder().setLaptop(laptop).build());

    laptop = laptop.toBuilder().setBrand("Hello").build();
    UpdateLaptopResponse response = stub.updateLaptop(request);

    assertNotNull(response);
    assertEquals(laptop.getId(), response.getLaptop().getId());
  }
}