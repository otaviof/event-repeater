package otaviof.github.io.eventrepeater.repeater;

import io.opentracing.Tracer;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import otaviof.github.io.eventrepeater.config.KafkaConfig;
import otaviof.github.io.eventrepeater.config.RepeaterConfig;
import otaviof.github.io.eventrepeater.kafka.AvroConsumer;
import otaviof.github.io.eventrepeater.kafka.AvroProducer;

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

