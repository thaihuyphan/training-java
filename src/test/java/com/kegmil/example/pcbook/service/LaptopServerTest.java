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

import java.util.*;

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

  private List<Laptop> sampleLaptops()
  {
    // Create some laptops first
    Generator generator = new Generator();

    Laptop laptop1 = generator.newLaptop().toBuilder()
            .setName("laptop1")
            .setPriceUsd(1999)
            .setCpu(CPU.newBuilder()
                    .setMinGhz(1.9)
                    .setMaxGhz(2.5)
                    .setNumberCores(4))
            .setRam(Memory.newBuilder()
                    .setValue(8)
                    .setUnit(Memory.Unit.GIGABYTE)
                    .build())
            .build();
    Laptop laptop2 = generator.newLaptop().toBuilder()
            .setName("laptop2")
            .setPriceUsd(2999)
            .setCpu(CPU.newBuilder()
                    .setMinGhz(2.0)
                    .setMaxGhz(2.8)
                    .setNumberCores(8))
            .setRam(Memory.newBuilder()
                    .setValue(8)
                    .setUnit(Memory.Unit.GIGABYTE)
                    .build())
            .build();
    Laptop laptop3 = generator.newLaptop().toBuilder()
            .setName("laptop3")
            .setPriceUsd(3999)
            .setCpu(CPU.newBuilder()
                    .setMinGhz(2.5)
                    .setMaxGhz(3.2)
                    .setNumberCores(8))
            .setRam(Memory.newBuilder()
                    .setValue(16)
                    .setUnit(Memory.Unit.GIGABYTE)
                    .build())
            .build();
    Laptop laptop4 = generator.newLaptop().toBuilder()
            .setName("laptop4")
            .setPriceUsd(4999)
            .setCpu(CPU.newBuilder()
                    .setMinGhz(4.2)
                    .setMaxGhz(5.0)
                    .setNumberCores(20))
            .setRam(Memory.newBuilder()
                    .setValue(16)
                    .setUnit(Memory.Unit.GIGABYTE)
                    .build())
            .build();

    return Arrays.asList(laptop1, laptop2, laptop3, laptop4);
  }

  @Test
  public void searchLaptopWithNonEmptyResult()
  {

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);

    List<Laptop> laptopList = sampleLaptops();

    laptopList.forEach(laptop -> {
      try {
        store.save(laptop);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    Filter filter = Filter.newBuilder()
            .setMinCpuCores(8)
            .setMinCpuGhz(2.1)
            .setMaxPriceUsd(5000)
            .setMinRam(Memory.newBuilder()
                    .setValue(8)
                    .setUnit(Memory.Unit.GIGABYTE)
                    .build())
            .build();

    Iterator<SearchLaptopResponse> response = stub.searchLaptop(SearchLaptopRequest.newBuilder()
            .setFilter(filter)
            .build());

    List<String> results = Arrays.asList(laptopList.get(2).getId(), laptopList.get(3).getId());
    List<String> actual = new LinkedList<>();

    assertTrue(response.hasNext());
    while (response.hasNext())
    {
      SearchLaptopResponse laptopResponse = response.next();
      actual.add(laptopResponse.getLaptop().getId());
    }
    assertArrayEquals(results.stream().sorted().toArray(), actual.stream().sorted().toArray());
  }

  @Test
  public void searchLaptopWithEmptyResult()
  {

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);

    List<Laptop> laptopList = sampleLaptops();
    laptopList.forEach(laptop -> {
      try {
        store.save(laptop);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    Filter filter = Filter.newBuilder()
            .setMinCpuCores(8)
            .setMinCpuGhz(2.1)
            .setMaxPriceUsd(2000)
            .setMinRam(Memory.newBuilder()
                    .setValue(16)
                    .setUnit(Memory.Unit.GIGABYTE)
                    .build())
            .build();

    Iterator<SearchLaptopResponse> response = stub.searchLaptop(SearchLaptopRequest.newBuilder()
            .setFilter(filter)
            .build());
    assertFalse(response.hasNext());
  }

  @Test
  public void updateLaptopWithValidID() throws Exception
  {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();
    store.save(laptop);

    Laptop newLaptop = laptop.toBuilder().setBrand("DevMac").build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    stub.updateLaptop(UpdateLaptopRequest.newBuilder()
            .setLaptop(newLaptop)
            .build());

    assertEquals("DevMac", store.find(laptop.getId()).getBrand());
  }

  @Test(expected = StatusRuntimeException.class)
  public void updateLaptopWithNonExistID() throws Exception
  {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();

    Laptop newLaptop = laptop.toBuilder().setId("HelloNewID").setBrand("DevMac").build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    stub.updateLaptop(UpdateLaptopRequest.newBuilder()
            .setLaptop(newLaptop)
            .build());
  }

  @Test
  public void deleteLaptopWithValidID() throws Exception
  {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop();
    store.save(laptop);

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    stub.deleteLaptop(DeleteLaptopRequest.newBuilder()
            .setId(laptop.getId())
            .build());

    assertNull(store.find(laptop.getId()));
  }

  @Test(expected = StatusRuntimeException.class)
  public void deleteLaptopWithNonExistID() throws Exception
  {
    Generator generator = new Generator();
    Laptop laptop = generator.newLaptop().toBuilder()
            .setId("HelloAnotherID")
            .build();

    LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
    stub.deleteLaptop(DeleteLaptopRequest.newBuilder()
            .setId(laptop.getId())
            .build());
  }
}