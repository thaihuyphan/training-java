package com.kegmil.example.pcbook;

import com.kegmil.example.pcbook.pb.Filter;
import com.kegmil.example.pcbook.pb.Memory;
import com.kegmil.example.pcbook.sample.Generator;
import com.kegmil.example.pcbook.serializer.Serializer;
import com.kegmil.example.pcbook.service.InMemoryLaptopStore;
import com.kegmil.example.pcbook.service.LaptopClient;
import com.kegmil.example.pcbook.service.LaptopServer;
import org.junit.Assert;
import org.junit.Test;
import com.kegmil.example.pcbook.pb.Laptop;

import java.io.IOException;
import java.util.List;

public class SerializerTest {

  @Test
  public void writeAndReadBinaryFile() throws IOException {
    String binaryFile = "laptop.bin";
    Laptop laptop1 = new Generator().newLaptop();

    Serializer serializer = new Serializer();
    serializer.writeBinaryFile(laptop1, binaryFile);

    Laptop laptop2 = serializer.readBinaryFile(binaryFile);
    Assert.assertEquals(laptop1, laptop2);
  }

  @Test
  public void TestSearchCase1() throws IOException {

    LaptopClient client = new LaptopClient("0.0.0.0", 6565);
    Serializer serializer = new Serializer();

    String binaryFile = "laptop.bin";
    Laptop laptop1 = serializer.readBinaryFile(binaryFile);

    client.createLaptop(laptop1);

    Memory minRam = Memory.newBuilder().setValue(8).setUnit(Memory.Unit.GIGABYTE).build();
    Filter filter = Filter.newBuilder()
            .setMaxPriceUsd(3000)
            .setMinCpuCores(4)
            .setMinCpuGhz(2.5)
            .setMinRam(minRam)
            .build();

    List<Laptop> arr = client.searchLaptop(filter);
    Assert.assertEquals(arr.get(0),laptop1);
  }

  @Test
  public void TestSearchCase2() throws IOException, InterruptedException {

    LaptopClient client = new LaptopClient("0.0.0.0", 6565);

    String binaryFile = "laptop.bin";
    Laptop laptop1 = new Generator().newLaptop();

    Serializer serializer = new Serializer();
    serializer.writeBinaryFile(laptop1, binaryFile);
    client.createLaptop(laptop1);

    Memory minRam = Memory.newBuilder().setValue(8).setUnit(Memory.Unit.GIGABYTE).build();
    Filter filter = Filter.newBuilder()
            .setMaxPriceUsd(1000)
            .setMinCpuCores(4)
            .setMinCpuGhz(2.5)
            .setMinRam(minRam)
            .build();

    List<Laptop> arr = client.searchLaptop(filter);
    Assert.assertTrue(arr.isEmpty());
  }

  @Test
  public void TestDeleteCase1() throws IOException, InterruptedException {


    LaptopClient client = new LaptopClient("0.0.0.0", 6565);

    String binaryFile = "laptop.bin";
    Laptop laptop1 = new Generator().newLaptop();

    Serializer serializer = new Serializer();
    serializer.writeBinaryFile(laptop1, binaryFile);
    client.createLaptop(laptop1);

    Laptop laptopRes = client.deleteLaptop(laptop1.getId());
    Assert.assertEquals(laptop1, laptopRes);
  }

  @Test
  public void TestDeleteCase2() throws IOException, InterruptedException {


    LaptopClient client = new LaptopClient("0.0.0.0", 6565);

    String binaryFile = "laptop.bin";
    Laptop laptop1 = new Generator().newLaptop();

    Serializer serializer = new Serializer();
    serializer.writeBinaryFile(laptop1, binaryFile);
    client.createLaptop(laptop1);

    Laptop laptopRes = client.deleteLaptop("12345");
    Assert.assertEquals(null, laptopRes);

  }

  @Test
  public void TestUpdateCase1() throws IOException, InterruptedException {



    LaptopClient client = new LaptopClient("0.0.0.0", 6565);

    String binaryFile1 = "laptop.bin";
    Laptop laptop1 = new Generator().newLaptop();
    String binaryFile2 = "laptop_update.bin";
    Laptop laptop2 = new Generator().newLaptop();

    Serializer serializer = new Serializer();

    serializer.writeBinaryFile(laptop1, binaryFile1);
    client.createLaptop(laptop1);

    serializer.writeBinaryFile(laptop2, binaryFile2);
    client.createLaptop(laptop2);

    Laptop laptopRes = client.updateLaptop(laptop2);
    Assert.assertEquals(laptop2, laptopRes);

  }

  @Test
  public void TestUpdateCase2() throws IOException, InterruptedException {



    LaptopClient client = new LaptopClient("0.0.0.0", 6565);

    String binaryFile1 = "laptop.bin";
    Laptop laptop1 = new Generator().newLaptop();

    Serializer serializer = new Serializer();
    serializer.writeBinaryFile(laptop1, binaryFile1);
    client.createLaptop(laptop1);

    Laptop laptop2 = new Generator().newLaptop();

    Laptop laptopRes = client.updateLaptop(laptop2);
    Assert.assertEquals(null, laptopRes);

  }
}