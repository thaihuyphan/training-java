package com.kegmil.example.pcbook.serializer;

import com.google.protobuf.util.JsonFormat;
import com.kegmil.example.pcbook.pb.Laptop;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Serializer {

  public void writeBinaryFile(Laptop laptop, String fileName) throws IOException {
    FileOutputStream outputStream = new FileOutputStream(fileName);
    laptop.writeTo(outputStream);
    outputStream.close();
  }

  public Laptop readBinaryFile(String fileName) throws IOException {
    FileInputStream inStream = new FileInputStream(fileName);
    Laptop laptop = Laptop.parseFrom(inStream);
    inStream.close();
    return laptop;
  }

  public void writeJSONFile(Laptop laptop, String fileName) throws IOException {
    JsonFormat.Printer printer = JsonFormat.printer()
        .includingDefaultValueFields()
        .preservingProtoFieldNames();

    String jsonString = printer.print(laptop);

    FileOutputStream outStream = new FileOutputStream(fileName);
    outStream.write(jsonString.getBytes());
    outStream.close();
  }

  public static void main(String[] args) throws IOException {
    Serializer serializer = new Serializer();
    Laptop laptop = serializer.readBinaryFile("laptop.bin");
    serializer.writeJSONFile(laptop, "laptop.json");
  }
}
