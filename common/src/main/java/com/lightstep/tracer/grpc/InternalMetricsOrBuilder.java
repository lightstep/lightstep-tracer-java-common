// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: collector.proto

package com.lightstep.tracer.grpc;

public interface InternalMetricsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:lightstep.collector.InternalMetrics)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.google.protobuf.Timestamp start_timestamp = 1;</code>
   */
  boolean hasStartTimestamp();
  /**
   * <code>.google.protobuf.Timestamp start_timestamp = 1;</code>
   */
  com.google.protobuf.Timestamp getStartTimestamp();
  /**
   * <code>.google.protobuf.Timestamp start_timestamp = 1;</code>
   */
  com.google.protobuf.TimestampOrBuilder getStartTimestampOrBuilder();

  /**
   * <code>uint64 duration_micros = 2;</code>
   */
  long getDurationMicros();

  /**
   * <code>repeated .lightstep.collector.Log logs = 3;</code>
   */
  java.util.List<com.lightstep.tracer.grpc.Log> 
      getLogsList();
  /**
   * <code>repeated .lightstep.collector.Log logs = 3;</code>
   */
  com.lightstep.tracer.grpc.Log getLogs(int index);
  /**
   * <code>repeated .lightstep.collector.Log logs = 3;</code>
   */
  int getLogsCount();
  /**
   * <code>repeated .lightstep.collector.Log logs = 3;</code>
   */
  java.util.List<? extends com.lightstep.tracer.grpc.LogOrBuilder> 
      getLogsOrBuilderList();
  /**
   * <code>repeated .lightstep.collector.Log logs = 3;</code>
   */
  com.lightstep.tracer.grpc.LogOrBuilder getLogsOrBuilder(
      int index);

  /**
   * <code>repeated .lightstep.collector.MetricsSample counts = 4;</code>
   */
  java.util.List<com.lightstep.tracer.grpc.MetricsSample> 
      getCountsList();
  /**
   * <code>repeated .lightstep.collector.MetricsSample counts = 4;</code>
   */
  com.lightstep.tracer.grpc.MetricsSample getCounts(int index);
  /**
   * <code>repeated .lightstep.collector.MetricsSample counts = 4;</code>
   */
  int getCountsCount();
  /**
   * <code>repeated .lightstep.collector.MetricsSample counts = 4;</code>
   */
  java.util.List<? extends com.lightstep.tracer.grpc.MetricsSampleOrBuilder> 
      getCountsOrBuilderList();
  /**
   * <code>repeated .lightstep.collector.MetricsSample counts = 4;</code>
   */
  com.lightstep.tracer.grpc.MetricsSampleOrBuilder getCountsOrBuilder(
      int index);

  /**
   * <code>repeated .lightstep.collector.MetricsSample gauges = 5;</code>
   */
  java.util.List<com.lightstep.tracer.grpc.MetricsSample> 
      getGaugesList();
  /**
   * <code>repeated .lightstep.collector.MetricsSample gauges = 5;</code>
   */
  com.lightstep.tracer.grpc.MetricsSample getGauges(int index);
  /**
   * <code>repeated .lightstep.collector.MetricsSample gauges = 5;</code>
   */
  int getGaugesCount();
  /**
   * <code>repeated .lightstep.collector.MetricsSample gauges = 5;</code>
   */
  java.util.List<? extends com.lightstep.tracer.grpc.MetricsSampleOrBuilder> 
      getGaugesOrBuilderList();
  /**
   * <code>repeated .lightstep.collector.MetricsSample gauges = 5;</code>
   */
  com.lightstep.tracer.grpc.MetricsSampleOrBuilder getGaugesOrBuilder(
      int index);
}
