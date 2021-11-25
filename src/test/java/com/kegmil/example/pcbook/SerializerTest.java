package com.kegmil.example.pcbook;

import com.kegmil.example.pcbook.sample.Generator;
import com.kegmil.example.pcbook.serializer.Serializer;
import org.junit.Assert;
import org.junit.Test;
import com.kegmil.example.pcbook.pb.Laptop;

import java.io.IOException;

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
}