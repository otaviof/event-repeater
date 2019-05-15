package otaviof.github.io.eventrepeater.kafka;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerializer;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingKafkaProducer;
import io.opentracing.contrib.kafka.TracingProducerInterceptor;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import otaviof.github.io.eventrepeater.config.KafkaConfig;
import otaviof.github.io.eventrepeater.config.RepeaterConfig;

@Slf4j
public class AvroProducer {
    private final KafkaConfig kafkaConfig;
    private final RepeaterConfig repeaterConfig;

    private final TracingKafkaProducer<String, GenericRecord> tracingProducer;
    private final KafkaProducer<String, GenericRecord> producer;

    public AvroProducer(Tracer tracer, KafkaConfig kafkaConfig, RepeaterConfig repeaterConfig) {
        this.kafkaConfig = kafkaConfig;
        this.repeaterConfig = repeaterConfig;

        this.producer = new KafkaProducer<String, GenericRecord>(producerProperties());
        this.tracingProducer = new TracingKafkaProducer<>(producer, tracer);
    }

    private Properties producerProperties() {
        var p = new Properties();

        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBrokers());
        p.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                kafkaConfig.getSchemaRegistryUrl());

        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GenericAvroSerializer.class.getName());
        p.put(ProducerConfig.ACKS_CONFIG, "all");

        p.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());

        return p;
    }

    public void send(String k, GenericRecord v, Map<String, String> headers) throws
            ExecutionException, InterruptedException {
        var record = new ProducerRecord<>(repeaterConfig.getTo(), k, v);

        log.info("Producing record, key='{}', value='{}'", k, v.toString());

        headers.forEach((key, value) -> record.headers().add(key, value.getBytes()));

        // tracingProducer.send(record).get();
        producer.send(record).get();
    }
}