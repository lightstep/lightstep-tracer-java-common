# Development Notes

## Makefile

`Makefile`s are used to encapsulate the various tools in the toolchain:

```bash
make build      # builds common library
make publish    # increment versions and publish the artifact to bintray
```

NOTE: To publish, see the [Release documentation](RELEASE.md).

###  Directory structure

```
Makefile                    # Top-level Makefile to encapsulate tool-specifics
common/                     # Shared source code for JRE and Android
okhttp/                     # protobuf over http transport
grpc/                       # grpc transport
```

## Formatting

The project uses the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) as a formatting standard. Configurations for your IDE can be downloaded from [github.com/google/styleguide](https://github.com/google/styleguide).

## Architecture

For a general overview of Ligthstep Tracers, see [Lightstep Tracers Spec](https://github.com/lightstep/specs).

This repository contains the very core tracing functionality for Lightstep Tracers,
and it must be used via either the standard Java or the Android layers. See more below.

Three artifacts are produced here:

* **com.lightstep.tracer:java-common**: Core Tracer functionality.
* **com.lightstep.tracer:tracer-okhttp**: Transport layer for protobuf over http.
* **com.lightstep.tracer:tracer-grpc**: Transport layer for gRPC.

Core Tracer functionality and ONE transport layer needs to be imported when
performing actual tracing.

The following client tracers use the artifacts mentioned above:

* **com.lightstep.tracer:lightstep-tracer-java**: [Standard Java Tracer](https://github.com/lightstep/lightstep-tracer-java).
* **com.lightstep.tracer:lightstep-tracer-android**: [Android Tracer](https://github.com/lightstep/lightstep-tracer-android).

Examples of imports are then:

* lightstep-tracer-java + tracer-grpc: Java tracing using http/protobuf transport.
* lightstep-tracer-android + tracer-okhttp: Android tracing using http/protobuf transport.

In both examples lighstep-tracer-java and lightstep-tracer-android implicitly
import java-common.

### Deployment

Deployment is automatically handled by pushing a tag, as explained in [RELEASE.md](Release.md).
As the artifacts produced here are consumed by both the Standard Java and Android Tracers,
the deployment order is:

1) Publish artifacts from this repo, based on a new version, e.g. 0.35.0 or 0.34.7.
   After some minutes they should be visible in Maven Central.
2) In the Standard Java and Android repos, update the version of the dependency
   to all tracer-common, tracer-okhttp and tracer-grpc. Make sure the build is green.
   Merge changes.
3) Publish artifacts from both the Standard Java and Android repos.

Observe that the artifacts of all the repos have matching minor versions, in order
to simplify version tracking, e.g. tracer-java 0.35.0 depends on tracer-common 0.35.0,
and an updated tracer 0.35.0 can depend on either tracer-common 0.35.0 or 0.35.1 (if changes
were published in the latter).
