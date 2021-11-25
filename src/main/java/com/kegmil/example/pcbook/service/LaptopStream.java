package com.kegmil.example.pcbook.service;

import com.kegmil.example.pcbook.pb.Laptop;

public interface LaptopStream {
  void send(Laptop laptop);
}
