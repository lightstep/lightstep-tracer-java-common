package com.lightstep.tracer.shared;

import com.lightstep.tracer.grpc.ReportRequest;
import com.lightstep.tracer.grpc.ReportResponse;

class HttpCollectorClient extends CollectorClient {
  HttpCollectorClient() {}

  @Override
  ReportResponse report(ReportRequest request) {
    return ReportResponse.getDefaultInstance();
  }

  @Override
  void reconnect() {}

  @Override
  void shutdown() {}
}
