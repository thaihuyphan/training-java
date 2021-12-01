package com.kegmil.example.pcbook.service;

import com.kegmil.example.pcbook.pb.CreateLaptopResponse;
import com.kegmil.example.pcbook.pb.CreateLaptopRequest;
import com.kegmil.example.pcbook.pb.SearchLaptopResponse;
import com.kegmil.example.pcbook.pb.SearchLaptopRequest;
import com.kegmil.example.pcbook.pb.UpdateLaptopResponse;
import com.kegmil.example.pcbook.pb.UpdateLaptopRequest;
import com.kegmil.example.pcbook.pb.DeleteLaptopResponse;
import com.kegmil.example.pcbook.pb.DeleteLaptopRequest;
import com.kegmil.example.pcbook.pb.LaptopServiceGrpc;
import com.kegmil.example.pcbook.pb.Laptop;
import com.kegmil.example.pcbook.pb.Filter;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.UUID;
import java.util.logging.Logger;

public class LaptopService extends LaptopServiceGrpc.LaptopServiceImplBase {

  private static final Logger logger = Logger.getLogger(LaptopService.class.getName());

  private LaptopStore laptopStore;

  public LaptopService(LaptopStore laptopStore) {
    this.laptopStore = laptopStore;
  }

  @Override
  public void createLaptop(CreateLaptopRequest request, StreamObserver<CreateLaptopResponse> responseObserver) {
    Laptop laptop = request.getLaptop();
    String id = laptop.getId();
    logger.info("Get a create-laptop request with ID: " + id);

    UUID uuid;
    if (id.isEmpty()) {
      uuid = UUID.randomUUID();
    } else {
      try {
        uuid = UUID.fromString(id);
      } catch (IllegalArgumentException e) {
        responseObserver.onError(
            Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asRuntimeException());
        return;
      }
    }

    if (Context.current().isCancelled()) {
      logger.info("Request is cancelled");
      responseObserver.onError(Status.CANCELLED.withDescription("Request is cancelled").asRuntimeException());
    }

    Laptop other = laptop.toBuilder().setId(uuid.toString()).build();
    try {
      laptopStore.save(other);
    } catch (AlreadyExistException e) {
      responseObserver.onError(
          Status.ALREADY_EXISTS
              .withDescription(e.getMessage())
              .asRuntimeException());
      return;
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
      return;
    }

    CreateLaptopResponse response = CreateLaptopResponse.newBuilder().setId(other.getId()).build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();

    logger.info("Saved laptop with ID: " + other.getId());
  }

  @Override
  public void searchLaptop(SearchLaptopRequest request, StreamObserver<SearchLaptopResponse> responseObserver) {
    Filter filter = request.getFilter();
    logger.info("Got a search-laptop request with filter:\n" + filter);

    laptopStore.search(filter, laptop -> {
      logger.info("Found laptop with ID: " + laptop.getId());
      SearchLaptopResponse response = SearchLaptopResponse.newBuilder().setLaptop(laptop).build();
      responseObserver.onNext(response);
    });

    responseObserver.onCompleted();
    logger.info("Search laptop completed");
  }

  @Override
  public void updateLaptop(UpdateLaptopRequest request, StreamObserver<UpdateLaptopResponse> responseObserver) {

    try
      {
        Laptop updatedLaptop = laptopStore.update(request.getLaptop());
        responseObserver.onNext(UpdateLaptopResponse.newBuilder().setLaptop(updatedLaptop).build());
        responseObserver.onCompleted();
      }
      catch (NotExistException ne)
      {
        responseObserver.onError(
                Status.NOT_FOUND.withDescription(ne.getMessage()).asRuntimeException()
        );
      }
      catch (Exception e)
      {
        responseObserver.onError(
                Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException()
        );
      }
  }

  @Override
  public void deleteLaptop(DeleteLaptopRequest request, StreamObserver<DeleteLaptopResponse> responseObserver) {
    try
    {
      laptopStore.delete(request.getId());
      responseObserver.onNext(DeleteLaptopResponse.newBuilder().build());
      responseObserver.onCompleted();
    }
    catch (NotExistException ne)
    {
      responseObserver.onError(
              Status.NOT_FOUND.withDescription(ne.getMessage()).asRuntimeException()
      );
    }
    catch (Exception e)
    {
      responseObserver.onError(
              Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException()
      );
    }

  }
}
