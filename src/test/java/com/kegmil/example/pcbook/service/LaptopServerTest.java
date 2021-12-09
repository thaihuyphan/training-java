package com.kegmil.example.pcbook.service;

import com.kegmil.example.pcbook.pb.*;
import com.kegmil.example.pcbook.sample.Generator;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.*;
import java.util.stream.Stream;

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
        server = new LaptopServer(serverBuilder, 6565, store);
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

    @Test(expected = StatusRuntimeException.class)
    public void updateLaptopWithEmptyLaptop() {
        Laptop laptop = Laptop.newBuilder().build();
        UpdateLaptopRequest request = UpdateLaptopRequest.newBuilder().setLaptop(laptop).build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        UpdateLaptopResponse response = stub.updateLaptop(request);
    }

    @Test(expected = StatusRuntimeException.class)
    public void updateLaptopWithNotExistID() {
        Generator generator = new Generator();
        Laptop laptop = generator.newLaptop();
        UpdateLaptopRequest request = UpdateLaptopRequest.newBuilder().setLaptop(laptop).build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        UpdateLaptopResponse response = stub.updateLaptop(request);
    }

    @Test
    public void updateLaptopWithValidLaptop() throws Exception {
        Generator generator = new Generator();
        Laptop laptop = generator.newLaptop();
        store.save(laptop);

        laptop = laptop.toBuilder().setName("This is a very beautiful name").setBrand("Lenovo").build();
        UpdateLaptopRequest request = UpdateLaptopRequest.newBuilder().setLaptop(laptop).build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        UpdateLaptopResponse response = stub.updateLaptop(request);

        assertNotNull(response);
        assertEquals(laptop, store.find(laptop.getId()));
    }

    @Test(expected = StatusRuntimeException.class)
    public void deleteLaptopWithEmptyID() {
        DeleteLaptopRequest request = DeleteLaptopRequest.newBuilder().setId("").build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        DeleteLaptopResponse response = stub.deleteLaptop(request);
    }

    @Test(expected = StatusRuntimeException.class)
    public void deleteLaptopWithNotExistID() {
        Generator generator = new Generator();
        Laptop laptop = generator.newLaptop();
        DeleteLaptopRequest request = DeleteLaptopRequest.newBuilder().setId(laptop.getId()).build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        DeleteLaptopResponse response = stub.deleteLaptop(request);
    }

    @Test
    public void deleteLaptopWithValidID() throws Exception {
        Generator generator = new Generator();
        Laptop laptop = generator.newLaptop();
        store.save(laptop);

        DeleteLaptopRequest request = DeleteLaptopRequest.newBuilder().setId(laptop.getId()).build();
        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        DeleteLaptopResponse response = stub.deleteLaptop(request);

        assertNull(store.find(laptop.getId()));
    }

    @Test
    public void searchLaptopForExistResult() throws Exception {
        Generator generator = new Generator();

        //Laptop1
        Laptop laptop1 = Laptop.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("laptop1")
                .setPriceUsd(4000)
                .setCpu(CPU.newBuilder()
                        .setNumberCores(6)
                        .setMinGhz(3.0))
                .setRam(Memory.newBuilder()
                        .setValue(64)
                        .setUnit(Memory.Unit.GIGABYTE))
                .build();
        store.save(laptop1);

        //Laptop2
        Laptop laptop2 = Laptop.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("laptop2")
                .setPriceUsd(6000)
                .setCpu(CPU.newBuilder()
                        .setNumberCores(8)
                        .setMinGhz(3.5))
                .setRam(Memory.newBuilder()
                        .setValue(64)
                        .setUnit(Memory.Unit.GIGABYTE))
                .build();
        store.save(laptop2);

        //Laptop3
        Laptop laptop3 = Laptop.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("laptop3")
                .setPriceUsd(2000)
                .setCpu(CPU.newBuilder()
                        .setNumberCores(4)
                        .setMinGhz(2.5))
                .setRam(Memory.newBuilder()
                        .setValue(32)
                        .setUnit(Memory.Unit.GIGABYTE))
                .build();
        store.save(laptop3);

        //Laptop4
        Laptop laptop4 = Laptop.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("laptop4")
                .setPriceUsd(3500)
                .setCpu(CPU.newBuilder()
                        .setNumberCores(4)
                        .setMinGhz(3.0))
                .setRam(Memory.newBuilder()
                        .setValue(64)
                        .setUnit(Memory.Unit.GIGABYTE))
                .build();
        store.save(laptop4);

        //Laptop5
        Laptop laptop5 = Laptop.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("laptop5")
                .setPriceUsd(5000)
                .setCpu(CPU.newBuilder()
                        .setNumberCores(8)
                        .setMinGhz(3.0))
                .setRam(Memory.newBuilder()
                        .setValue(64)
                        .setUnit(Memory.Unit.GIGABYTE))
                .build();
        store.save(laptop5);

        Filter filter = Filter.newBuilder()
                .setMaxPriceUsd(4000)
                .setMinCpuCores(4)
                .setMinCpuGhz(3.0)
                .setMinRam(Memory.newBuilder().setValue(32).setUnit(Memory.Unit.GIGABYTE).build())
                .build();

        SearchLaptopRequest request = SearchLaptopRequest.newBuilder().setFilter(filter).build();
        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        Iterator<SearchLaptopResponse> response = stub.searchLaptop(request);

        List<Laptop> trueList = Arrays.asList(laptop1, laptop4);
        boolean check = true;
        while(response.hasNext()){
            Laptop laptop = response.next().getLaptop();
            if(!trueList.contains(laptop)){
                check = false;
            }
        }

        assertEquals(true, check);
    }

    @Test
    public void searchLaptopForEmptyResult() throws Exception{
        Generator generator = new Generator();

        //Laptop1
        Laptop laptop1 = Laptop.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("laptop1")
                .setPriceUsd(4000)
                .setCpu(CPU.newBuilder()
                        .setNumberCores(6)
                        .setMinGhz(3.0))
                .setRam(Memory.newBuilder()
                        .setValue(64)
                        .setUnit(Memory.Unit.GIGABYTE))
                .build();
        store.save(laptop1);

        //Laptop2
        Laptop laptop2 = Laptop.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("laptop2")
                .setPriceUsd(6000)
                .setCpu(CPU.newBuilder()
                        .setNumberCores(8)
                        .setMinGhz(3.5))
                .setRam(Memory.newBuilder()
                        .setValue(64)
                        .setUnit(Memory.Unit.GIGABYTE))
                .build();
        store.save(laptop2);

        //Laptop3
        Laptop laptop3 = Laptop.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("laptop3")
                .setPriceUsd(2000)
                .setCpu(CPU.newBuilder()
                        .setNumberCores(4)
                        .setMinGhz(2.5))
                .setRam(Memory.newBuilder()
                        .setValue(32)
                        .setUnit(Memory.Unit.GIGABYTE))
                .build();
        store.save(laptop3);

        //Laptop4
        Laptop laptop4 = Laptop.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("laptop4")
                .setPriceUsd(3500)
                .setCpu(CPU.newBuilder()
                        .setNumberCores(4)
                        .setMinGhz(3.0))
                .setRam(Memory.newBuilder()
                        .setValue(64)
                        .setUnit(Memory.Unit.GIGABYTE))
                .build();
        store.save(laptop4);

        //Laptop5
        Laptop laptop5 = Laptop.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("laptop5")
                .setPriceUsd(5000)
                .setCpu(CPU.newBuilder()
                        .setNumberCores(8)
                        .setMinGhz(3.0))
                .setRam(Memory.newBuilder()
                        .setValue(64)
                        .setUnit(Memory.Unit.GIGABYTE))
                .build();
        store.save(laptop5);

        Filter filter = Filter.newBuilder()
                .setMaxPriceUsd(3000)
                .setMinCpuCores(8)
                .setMinCpuGhz(3.5)
                .setMinRam(Memory.newBuilder().setValue(64).setUnit(Memory.Unit.GIGABYTE).build())
                .build();
        SearchLaptopRequest request = SearchLaptopRequest.newBuilder().setFilter(filter).build();
        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        Iterator<SearchLaptopResponse> response = stub.searchLaptop(request);

        assertEquals(false, response.hasNext());
    }

}