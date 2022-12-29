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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class LaptopServerDeleteTest {


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
    public void deleteLaptopWithAValidID() throws Exception {
        Generator generator = new Generator();
        Laptop laptop = generator.newLaptop();
        store.save(laptop);
        DeleteLaptopRequest request = DeleteLaptopRequest.newBuilder().setId(laptop.getId()).build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        DeleteLaptopResponse response = stub.deleteLaptopById(request);

        String expectedMessage = "Delete Success";
        String actualMessage = response.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test(expected = StatusRuntimeException.class)
    public void deleteLaptopWithAnInvalidID() {
        DeleteLaptopRequest request = DeleteLaptopRequest.newBuilder().setId("InValidId").build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        DeleteLaptopResponse response = stub.deleteLaptopById(request);


    }

    @Test(expected = StatusRuntimeException.class)
    public void deleteLaptopWithEmptyID() {
        DeleteLaptopRequest request = DeleteLaptopRequest.newBuilder().setId("").build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        DeleteLaptopResponse response = stub.deleteLaptopById(request);

    }



    @Test(expected = StatusRuntimeException.class)
    public void deleteLaptopWithIDDeleted() throws Exception {
        Generator generator = new Generator();
        Laptop laptop = generator.newLaptop();
        store.save(laptop);
        store.deleteById(laptop.getId());

        DeleteLaptopRequest request = DeleteLaptopRequest.newBuilder().setId(laptop.getId()).build();
        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        DeleteLaptopResponse response = stub.deleteLaptopById(request);

    }

    @Test()
    public void deleteLaptopWithListID() throws Exception {
        Generator generator = new Generator();
        Laptop laptop1 = generator.newLaptop();
        Laptop laptop2 = generator.newLaptop();
        Laptop laptop3 = generator.newLaptop();
        Laptop laptop4 = generator.newLaptop();
        store.save(laptop1);
        store.save(laptop2);
        store.save(laptop3);
        store.save(laptop4);

        DeleteLaptopByManyIdRequest request = DeleteLaptopByManyIdRequest.newBuilder().addListId(laptop1.getId())
                .addListId(laptop2.getId()).addListId(laptop3.getId()).addListId(laptop4.getId()).build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        DeleteLaptopByManyIdResponse response = stub.deleteLaptopByManyId(request);

        String expectedResult = "Deleted " + 4;
        String actualResult = response.getMessage();

        assertTrue(actualResult.contains(expectedResult));

    }

    @Test(expected = StatusRuntimeException.class)
    public void deleteLaptopByListIDsHaveInvalidID() throws Exception {
        Generator generator = new Generator();
        Laptop laptop1 = generator.newLaptop();
        Laptop laptop2 = generator.newLaptop();
        Laptop laptop3 = generator.newLaptop();
        Laptop laptop4 = generator.newLaptop();
        store.save(laptop1);
        store.save(laptop2);
        store.save(laptop3);
        store.save(laptop4);


        DeleteLaptopByManyIdRequest request = DeleteLaptopByManyIdRequest.newBuilder().addListId(laptop1.getId())
                .addListId(laptop2.getId()).addListId(laptop3.getId()).addListId(laptop4.getId()).addListId("invalid").build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        DeleteLaptopByManyIdResponse response = stub.deleteLaptopByManyId(request);

    }

    @Test(expected = StatusRuntimeException.class)
    public void deleteLaptopByListIDsHaveEmptyID() throws Exception {
        Generator generator = new Generator();
        Laptop laptop1 = generator.newLaptop();
        Laptop laptop2 = generator.newLaptop();
        Laptop laptop3 = generator.newLaptop();
        Laptop laptop4 = generator.newLaptop();
        store.save(laptop1);
        store.save(laptop2);
        store.save(laptop3);
        store.save(laptop4);


        DeleteLaptopByManyIdRequest request = DeleteLaptopByManyIdRequest.newBuilder().addListId(laptop1.getId())
                .addListId(laptop2.getId()).addListId(laptop3.getId()).addListId(laptop4.getId()).addListId("").build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        DeleteLaptopByManyIdResponse response = stub.deleteLaptopByManyId(request);

    }

    @Test
    public void deleteLaptopByFilter() throws Exception {

        Laptop laptop1 = Laptop.newBuilder().setId("ec9bbd8a-f9ad-48d4-93d7-e0b25b8d0f41")
                .setPriceUsd(1)
                .setCpu(CPU.newBuilder().setNumberCores(190).setMinGhz(1.4))
                .setRam(Memory.newBuilder().setValue(190).build()).build();

        Laptop laptop2 = Laptop.newBuilder().setId("ec9bbd8a-f9ad-48d4-93d7-e0b25b8d0f47")
                .setPriceUsd(1)
                .setCpu(CPU.newBuilder().setNumberCores(190).setMinGhz(1.4))
                .setRam(Memory.newBuilder().setValue(190).build()).build();

        Laptop laptop3 = Laptop.newBuilder().setId("ec9bbd8a-f9ad-48d4-93d7-e0b25b8d0f43")
                .setPriceUsd(1)
                .setCpu(CPU.newBuilder().setNumberCores(190).setMinGhz(1.4))
                .setRam(Memory.newBuilder().setValue(190).build()).build();

        Laptop laptop4 = Laptop.newBuilder().setId("ec9bbd8a-f9ad-48d4-93d7-e0b25b8d0f27")
                .setPriceUsd(20)
                .setCpu(CPU.newBuilder().setNumberCores(190).setMinGhz(1.4))
                .setRam(Memory.newBuilder().setValue(190).build()).build();


        store.save(laptop1);
        store.save(laptop2);
        store.save(laptop3);
        store.save(laptop4);

        Filter filter = Filter.newBuilder().setMaxPriceUsd(13).setMinCpuGhz(1.3).setMinCpuCores(100)
                .setMinRam(Memory.newBuilder().setValue(100).build()).build();

        DeleteLaptopByFilterRequest request = DeleteLaptopByFilterRequest.newBuilder().setFilter(filter).build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        DeleteLaptopByFilterResponse response = stub.deleteLaptopByFilter(request);

        String expectedResult = "Deleted " + 3;
        String actualResult = response.getMessage();

        assertTrue(actualResult.contains(expectedResult));

    }

}
