package com.kegmil.example.pcbook.service;

import com.kegmil.example.pcbook.pb.Filter;
import com.kegmil.example.pcbook.pb.Laptop;

public interface LaptopStore {
  void save(Laptop laptop) throws Exception;
  Laptop find(String id);
  void search(Filter filter, LaptopStream stream);
}

