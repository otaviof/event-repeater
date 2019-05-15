package otaviof.github.io.eventrepeater.kafka;

import static org.apache.kafka.streams.KafkaStreams.State.RUNNING;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.streams.TracingKafkaClientSupplier;
import java.util.Properties;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import otaviof.github.io.eventrepeater.config.KafkaConfig;
import otaviof.github.io.eventrepeater.config.RepeaterConfig;

@Slf4j
public class AvroConsumer implements Runnable {
    private final Tracer tracer;
    private final KafkaConfig kafkaConfig;
    private final RepeaterConfig repeaterConfig;
    private final AvroProducer producer;

    private KafkaStreams streams;

    public AvroConsumer(
            Tracer tracer,
            KafkaConfig kafkaConfig,
            RepeaterConfig repeaterConfig,
            AvroProducer producer) {
        this.tracer = tracer;
        this.kafkaConfig = kafkaConfig;
        this.repeaterConfig = repeaterConfig;
        this.producer = producer;

        build();
    }

    private Properties consumerProperties() {
        var p = new Properties();

        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBrokers());
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, UUID.randomUUID().toString());
        p.put(StreamsConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        p.put(StreamsConfig.consumerPrefix(ConsumerConfig.GROUP_ID_CONFIG), UUID.randomUUID().toString());

        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        p.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                kafkaConfig.getSchemaRegistryUrl());

        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GenericAvroSerde.class.getName());
        p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class.getName());

        return p;
    }

    private void build() {
        var builder = new StreamsBuilder();
        var topology = builder.build();
        var supplier = new TracingKafkaClientSupplier(tracer);

        topology.addSource("SOURCE", repeaterConfig.getFrom())
                .addProcessor("StreamProcessor",
                        () -> new StreamProcessor(producer, repeaterConfig.getDelayMs()),
                        "SOURCE");

        streams = new KafkaStreams(topology, consumerProperties(), supplier);
    }

    public boolean isRunning() {
        log.info("Consumer state on topic '{}': {}", repeaterConfig.getFrom(), streams.state());
        return streams.state() == RUNNING;
    }

    @Override
    public void run() {
        log.info("Starting consumer on topic '{}'", repeaterConfig.getFrom());
        streams.start();
    }
}
