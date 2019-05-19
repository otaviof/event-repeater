package io.github.otaviof.eventrepeater.repeater;

import io.github.otaviof.eventrepeater.config.KafkaConfig;
import io.github.otaviof.eventrepeater.config.RepeaterConfig;
import io.github.otaviof.eventrepeater.kafka.AvroConsumer;
import io.github.otaviof.eventrepeater.kafka.AvroProducer;
import io.opentracing.Tracer;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;

@Slf4j
class Repeater {
    Repeater(Tracer tracer, KafkaConfig kafkaConfig, RepeaterConfig repeaterConfig) {
        var producer = new AvroProducer(kafkaConfig, repeaterConfig);
        var consumer = new AvroConsumer(tracer, kafkaConfig, repeaterConfig, producer);
        var consumerThread = new Thread(consumer);

        consumerThread.start();

        Awaitility.await()
                .atMost(60, TimeUnit.SECONDS)
                .until(consumer::isRunning);
    }
}

