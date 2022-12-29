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

import static junit.framework.TestCase.assertNotNull;

public class LaptopServerUpdateTest {

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

    @Test()
    public void updateLaptopWithAValidID() throws Exception {
        Generator generator = new Generator();
        Laptop laptop = generator.newLaptop();
        store.save(laptop);

        UpdateLaptopRequest request = UpdateLaptopRequest.newBuilder().setLaptop(laptop.toBuilder().setBrand("Dell")).build();
        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);

        UpdateLaptopResponse response = stub.updateLaptop(request);
        assertNotNull(response);


    }

    @Test(expected = StatusRuntimeException.class)
    public void updateLaptopWithInvalidID() throws Exception {
        Generator generator = new Generator();
        Laptop laptop = generator.newLaptop();

        store.save(laptop);

        Laptop.Builder builder = laptop.toBuilder();
        builder.setId("Invalid");
        Laptop laptopUpdate = builder.build();

        UpdateLaptopRequest request = UpdateLaptopRequest.newBuilder().setLaptop(laptopUpdate.toBuilder().setBrand("Lenovo")).build();
        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);

        UpdateLaptopResponse response = stub.updateLaptop(request);

    }

    @Test(expected = StatusRuntimeException.class)
    public void updateLaptopWithEmptyID() throws Exception {
        Generator generator = new Generator();
        Laptop laptop = generator.newLaptop();

        store.save(laptop);

        Laptop.Builder builder = laptop.toBuilder();
        builder.setId("");
        Laptop laptopUpdate = builder.build();

        UpdateLaptopRequest request = UpdateLaptopRequest.newBuilder().setLaptop(laptopUpdate.toBuilder().setBrand("Dell")).build();
        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);

        UpdateLaptopResponse response = stub.updateLaptop(request);
        assertNotNull(response);


    }

    @Test(expected = StatusRuntimeException.class)
    public void updateLaptopWithUpdateID() throws Exception {
        Generator generator = new Generator();
        Laptop laptop = generator.newLaptop();
        store.save(laptop);

        UpdateLaptopRequest request = UpdateLaptopRequest.newBuilder().setLaptop(laptop.toBuilder().setId("Id is update")).build();
        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);

        UpdateLaptopResponse response = stub.updateLaptop(request);

    }


}
