// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: collector.proto

package com.lightstep.tracer.grpc;

public interface LogOrBuilder extends
    // @@protoc_insertion_point(interface_extends:lightstep.collector.Log)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.google.protobuf.Timestamp timestamp = 1;</code>
   */
  boolean hasTimestamp();
  /**
   * <code>.google.protobuf.Timestamp timestamp = 1;</code>
   */
  com.google.protobuf.Timestamp getTimestamp();
  /**
   * <code>.google.protobuf.Timestamp timestamp = 1;</code>
   */
  com.google.protobuf.TimestampOrBuilder getTimestampOrBuilder();

  /**
   * <code>repeated .lightstep.collector.KeyValue fields = 2;</code>
   */
  java.util.List<com.lightstep.tracer.grpc.KeyValue> 
      getFieldsList();
  /**
   * <code>repeated .lightstep.collector.KeyValue fields = 2;</code>
   */
  com.lightstep.tracer.grpc.KeyValue getFields(int index);
  /**
   * <code>repeated .lightstep.collector.KeyValue fields = 2;</code>
   */
  int getFieldsCount();
  /**
   * <code>repeated .lightstep.collector.KeyValue fields = 2;</code>
   */
  java.util.List<? extends com.lightstep.tracer.grpc.KeyValueOrBuilder> 
      getFieldsOrBuilderList();
  /**
   * <code>repeated .lightstep.collector.KeyValue fields = 2;</code>
   */
  com.lightstep.tracer.grpc.KeyValueOrBuilder getFieldsOrBuilder(
      int index);
}
