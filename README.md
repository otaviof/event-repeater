# `event-repeater`

Mocked application to listen to events, and repeat those in a different Kafka topic. This mock
represents a stream processor instrumented with OpenTracing, therefore you can track events
inside the consumer and producer instances.

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