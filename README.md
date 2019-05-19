<p align="center">
    <a alt="Docker-Cloud Build Status" href="https://hub.docker.com/r/otaviof/event-repeater">
        <img alt="Docker-Cloud Build Status" src="https://img.shields.io/docker/cloud/build/otaviof/event-repeater.svg">
    </a>
</p>

# `event-repeater`

Mocked application to listen to events, and repeat those in a different Kafka topic. This mock
represents a stream processor instrumented with OpenTracing, therefore you can track events
inside the consumer and producer instances.

Quick example:

``` bash
docker run otaviof/event-repeater:latest
```

## Configuration

``` yaml
eventrepeater:
  kafka:
    schemaRegistryUrl: http://schemaregistry.localtest.me:8681
    brokers: kafka.localtest.me:9092
  repeaters:
    - from: kafka_request_topic
      to: kafka_response_topic
```