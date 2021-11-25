// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: laptop_message.proto

package com.kegmil.example.pcbook.pb;

public interface LaptopOrBuilder extends
    // @@protoc_insertion_point(interface_extends:kegmil.pcbook.Laptop)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string id = 1;</code>
   * @return The id.
   */
  java.lang.String getId();
  /**
   * <code>string id = 1;</code>
   * @return The bytes for id.
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <code>string brand = 2;</code>
   * @return The brand.
   */
  java.lang.String getBrand();
  /**
   * <code>string brand = 2;</code>
   * @return The bytes for brand.
   */
  com.google.protobuf.ByteString
      getBrandBytes();

  /**
   * <code>string name = 3;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <code>string name = 3;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>.kegmil.pcbook.CPU cpu = 4;</code>
   * @return Whether the cpu field is set.
   */
  boolean hasCpu();
  /**
   * <code>.kegmil.pcbook.CPU cpu = 4;</code>
   * @return The cpu.
   */
  com.kegmil.example.pcbook.pb.CPU getCpu();
  /**
   * <code>.kegmil.pcbook.CPU cpu = 4;</code>
   */
  com.kegmil.example.pcbook.pb.CPUOrBuilder getCpuOrBuilder();

  /**
   * <code>.kegmil.pcbook.Memory ram = 5;</code>
   * @return Whether the ram field is set.
   */
  boolean hasRam();
  /**
   * <code>.kegmil.pcbook.Memory ram = 5;</code>
   * @return The ram.
   */
  com.kegmil.example.pcbook.pb.Memory getRam();
  /**
   * <code>.kegmil.pcbook.Memory ram = 5;</code>
   */
  com.kegmil.example.pcbook.pb.MemoryOrBuilder getRamOrBuilder();

  /**
   * <code>repeated .kegmil.pcbook.GPU gpus = 6;</code>
   */
  java.util.List<com.kegmil.example.pcbook.pb.GPU> 
      getGpusList();
  /**
   * <code>repeated .kegmil.pcbook.GPU gpus = 6;</code>
   */
  com.kegmil.example.pcbook.pb.GPU getGpus(int index);
  /**
   * <code>repeated .kegmil.pcbook.GPU gpus = 6;</code>
   */
  int getGpusCount();
  /**
   * <code>repeated .kegmil.pcbook.GPU gpus = 6;</code>
   */
  java.util.List<? extends com.kegmil.example.pcbook.pb.GPUOrBuilder> 
      getGpusOrBuilderList();
  /**
   * <code>repeated .kegmil.pcbook.GPU gpus = 6;</code>
   */
  com.kegmil.example.pcbook.pb.GPUOrBuilder getGpusOrBuilder(
      int index);

  /**
   * <code>repeated .kegmil.pcbook.Storage storages = 7;</code>
   */
  java.util.List<com.kegmil.example.pcbook.pb.Storage> 
      getStoragesList();
  /**
   * <code>repeated .kegmil.pcbook.Storage storages = 7;</code>
   */
  com.kegmil.example.pcbook.pb.Storage getStorages(int index);
  /**
   * <code>repeated .kegmil.pcbook.Storage storages = 7;</code>
   */
  int getStoragesCount();
  /**
   * <code>repeated .kegmil.pcbook.Storage storages = 7;</code>
   */
  java.util.List<? extends com.kegmil.example.pcbook.pb.StorageOrBuilder> 
      getStoragesOrBuilderList();
  /**
   * <code>repeated .kegmil.pcbook.Storage storages = 7;</code>
   */
  com.kegmil.example.pcbook.pb.StorageOrBuilder getStoragesOrBuilder(
      int index);

  /**
   * <code>.kegmil.pcbook.Screen screen = 8;</code>
   * @return Whether the screen field is set.
   */
  boolean hasScreen();
  /**
   * <code>.kegmil.pcbook.Screen screen = 8;</code>
   * @return The screen.
   */
  com.kegmil.example.pcbook.pb.Screen getScreen();
  /**
   * <code>.kegmil.pcbook.Screen screen = 8;</code>
   */
  com.kegmil.example.pcbook.pb.ScreenOrBuilder getScreenOrBuilder();

  /**
   * <code>.kegmil.pcbook.Keyboard keyboard = 9;</code>
   * @return Whether the keyboard field is set.
   */
  boolean hasKeyboard();
  /**
   * <code>.kegmil.pcbook.Keyboard keyboard = 9;</code>
   * @return The keyboard.
   */
  com.kegmil.example.pcbook.pb.Keyboard getKeyboard();
  /**
   * <code>.kegmil.pcbook.Keyboard keyboard = 9;</code>
   */
  com.kegmil.example.pcbook.pb.KeyboardOrBuilder getKeyboardOrBuilder();

  /**
   * <code>double weight_kg = 10;</code>
   * @return Whether the weightKg field is set.
   */
  boolean hasWeightKg();
  /**
   * <code>double weight_kg = 10;</code>
   * @return The weightKg.
   */
  double getWeightKg();

  /**
   * <code>double weight_lb = 11;</code>
   * @return Whether the weightLb field is set.
   */
  boolean hasWeightLb();
  /**
   * <code>double weight_lb = 11;</code>
   * @return The weightLb.
   */
  double getWeightLb();

  /**
   * <code>double price_usd = 12;</code>
   * @return The priceUsd.
   */
  double getPriceUsd();

  /**
   * <code>uint32 release_year = 13;</code>
   * @return The releaseYear.
   */
  int getReleaseYear();

  /**
   * <code>.google.protobuf.Timestamp updated_at = 14;</code>
   * @return Whether the updatedAt field is set.
   */
  boolean hasUpdatedAt();
  /**
   * <code>.google.protobuf.Timestamp updated_at = 14;</code>
   * @return The updatedAt.
   */
  com.google.protobuf.Timestamp getUpdatedAt();
  /**
   * <code>.google.protobuf.Timestamp updated_at = 14;</code>
   */
  com.google.protobuf.TimestampOrBuilder getUpdatedAtOrBuilder();

  public com.kegmil.example.pcbook.pb.Laptop.WeightCase getWeightCase();
}