package com.kegmil.example.pcbook.service;

import com.kegmil.example.pcbook.pb.Filter;
import com.kegmil.example.pcbook.pb.Laptop;
import com.kegmil.example.pcbook.pb.Memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class InMemoryLaptopStore implements LaptopStore {

  private ConcurrentMap<String, Laptop> data;

  public InMemoryLaptopStore() {
    data = new ConcurrentHashMap<>(0);
  }

  @Override
  public void save(Laptop laptop) throws Exception {
    if (data.containsKey(laptop.getId())) {
      throw new AlreadyExistException("laptop Id already exists");
    }

    // deep copy
    Laptop other = laptop.toBuilder().build();
    data.put(other.getId(), other);
  }

  @Override
  public Laptop find(String id) {
    if (!data.containsKey(id)) {
      return null;
    }

    // deep copy
    return data.get(id).toBuilder().build();
  }

  @Override
  public void search(Filter filter, LaptopStream stream) {
    for (Map.Entry<String, Laptop> entry : data.entrySet()) {
      Laptop laptop = entry.getValue();
      if (isQualified(filter, laptop)) {
        stream.send(laptop.toBuilder().build());
      }
    }
  }

  /*
  First need to find the laptop by id. If it has in data, update. Else throw exception not found
   */
  @Override
  public void update(Laptop laptop, LaptopStream stream) {

      if(data.containsKey(laptop.getId())){
        data.replace(laptop.getId(), laptop);
        stream.send((laptop.toBuilder().build()));
      }
      else {
        throw new NotFoundExistException("Not found laptop which have update");
      }

  }

  @Override
  public void deleteById(String id) {
    Laptop laptop = find(id);
    //kiem tra co laptop nay hay khong
    if(laptop != null){
      data.remove(laptop.getId(), laptop);

    }
    else{
      throw new NotFoundExistException("Don't have the laptop you need delete");
    }
  }

  @Override
  public int deleteByFilter(Filter filter) {
    List<Laptop> laptops = new ArrayList<Laptop>();

    search(filter, laptop -> {
      laptops.add(laptop);
    });
    int numberOfLaptopDeleted = laptops.size();
    laptops.forEach(laptop -> {
      if(data.containsKey(laptop.getId())){
        data.remove(laptop.getId());

      }
    });
    return numberOfLaptopDeleted;
  }

  @Override
  public AtomicInteger deleteByIds(Stream<String> ids) {
    AtomicInteger numberOfLaptopDeleted = new AtomicInteger();
    ids.forEach(id -> {
      if(data.containsKey(id)){
        data.remove(id);
        numberOfLaptopDeleted.getAndIncrement();
      }
    });
    return numberOfLaptopDeleted;
  }

  private boolean isQualified(Filter filter, Laptop laptop) {
    if (laptop.getPriceUsd() > filter.getMaxPriceUsd()) {
      return false;
    }

    if (laptop.getCpu().getNumberCores() < filter.getMinCpuCores()) {
      return false;
    }

    if (laptop.getCpu().getMinGhz() < filter.getMinCpuGhz()) {
      return false;
    }

    if (toBit(laptop.getRam()) < toBit(filter.getMinRam())) {
      return false;
    }

    return true;
  }

  private long toBit(Memory memory) {
    long value = memory.getValue();

    switch (memory.getUnit()) {
      case BIT:
        return value;
      case BYTE:
        return value << 3;
      case KILOBYTE:
        return value << 13;
      case MEGABYTE:
        return value << 23;
      case GIGABYTE:
        return value << 33;
      case TERABYTE:
        return value << 43;
      default:
        return 0;
    }
  }
}
