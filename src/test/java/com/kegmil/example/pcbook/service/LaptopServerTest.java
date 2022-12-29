package com.kegmil.example.pcbook.service;

import com.kegmil.example.pcbook.pb.CreateLaptopRequest;
import com.kegmil.example.pcbook.pb.CreateLaptopResponse;
import com.kegmil.example.pcbook.pb.DeleteLaptopRequest;
import com.kegmil.example.pcbook.pb.DeleteLaptopResponse;
import com.kegmil.example.pcbook.pb.Laptop;
import com.kegmil.example.pcbook.pb.LaptopServiceGrpc;
import com.kegmil.example.pcbook.pb.UpdateLaptopRequest;
import com.kegmil.example.pcbook.pb.UpdateLaptopResponse;
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
  public void deleteLaptopWithAValidID() throws Exception {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();
    store.save(laptop);

    DeleteLaptopRequest request = DeleteLaptopRequest.newBuilder().setId(laptop.getId()).build();
    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    DeleteLaptopResponse response = stub.deleteLaptop(request);
    assertNotNull(response);
    assertEquals(laptop.getId(), response.getId());

    Laptop found = store.find(response.getId());
    assertNull(found);
  }

  @Test(expected = StatusRuntimeException.class)
  public void deleteLaptopWithAnInvalidID() {
    DeleteLaptopRequest request = DeleteLaptopRequest.newBuilder().setId("Invalid").build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    DeleteLaptopResponse response = stub.deleteLaptop(request);
  }

  @Test(expected = StatusRuntimeException.class)
  public void deleteLaptopWithAnEmptyID() {
    DeleteLaptopRequest request = DeleteLaptopRequest.newBuilder().setId("").build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    DeleteLaptopResponse response = stub.deleteLaptop(request);
  }

  @Test
  public void updateLaptopWithAValidID() throws Exception {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();
    store.save(laptop);
    UpdateLaptopRequest request = UpdateLaptopRequest.newBuilder().setLaptop(laptop).build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    UpdateLaptopResponse response = stub.updateLaptop(request);
    assertNotNull(response);
    assertEquals(laptop.getId(), response.getLaptop().getId());

    Laptop found = store.find(response.getLaptop().getId());
    assertNotNull(found);
  }

  @Test(expected = StatusRuntimeException.class)
  public void updateLaptopWithAnInvalidID() {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();
    UpdateLaptopRequest request = UpdateLaptopRequest.newBuilder().setLaptop(laptop).build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    UpdateLaptopResponse response = stub.updateLaptop(request);
  }

  @Test(expected = StatusRuntimeException.class)
  public void updateLaptopWithAnEmptyID() {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();
    Laptop other = laptop.toBuilder().setId("").build();
    UpdateLaptopRequest request = UpdateLaptopRequest.newBuilder().setLaptop(other).build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    UpdateLaptopResponse response = stub.updateLaptop(request);
  }
}