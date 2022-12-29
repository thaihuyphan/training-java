package com.kegmil.example.pcbook.service;

import com.kegmil.example.pcbook.pb.Filter;
import com.kegmil.example.pcbook.pb.Laptop;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public interface LaptopStore {
  void save(Laptop laptop) throws Exception;
  Laptop find(String id);
  void search(Filter filter, LaptopStream stream);

  void update(Laptop laptop, LaptopStream stream);
  void deleteById(String id);

  int deleteByFilter(Filter filter);

  AtomicInteger deleteByIds(Stream<String> ids);
}

