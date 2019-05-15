package otaviof.github.io.eventrepeater.kafka;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.awaitility.Awaitility;

@Slf4j
public class StreamProcessor implements Processor<String, GenericRecord> {
    private final AvroProducer producer;
    private final int delayMs;

    private ProcessorContext context;

    public StreamProcessor(AvroProducer producer, int delayMs) {
        this.producer = producer;
        this.delayMs = delayMs;
    }

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public void process(String k, GenericRecord v) {
        var headers = new HashMap<String, String>();

        context.headers().forEach(h -> {
            var value = new String(h.value());
            log.info("Header -> '{}'='{}'", h.key(), value);
            headers.put(h.key(), value);
        });

        try {
            sleep();
            producer.send(k, v, headers);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        context.commit();
    }

    @Override
    public void close() {
        // no implementation
    }

    private void sleep() {
        if (delayMs == 0) {
            return;
        }
        log.info("Sleeping for '{}' ms...", delayMs);
        Awaitility.await().atLeast(delayMs, TimeUnit.MILLISECONDS);
    }
}
