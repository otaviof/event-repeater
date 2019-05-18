package otaviof.github.io.eventrepeater.kafka;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingKafkaUtils;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.awaitility.Awaitility;

@Slf4j
public class StreamProcessor implements Processor<String, GenericRecord> {
    private final Tracer tracer;
    private final AvroProducer producer;
    private final int delayMs;

    private ProcessorContext context;

    StreamProcessor(Tracer tracer, AvroProducer producer, int delayMs) {
        this.tracer = tracer;
        this.producer = producer;
        this.delayMs = delayMs;
    }


    @Override
    public void init(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public void process(String k, GenericRecord v) {
        var span = tracingSpan();

        try (var scope = tracer.scopeManager().activate(span)) {
            var headers = new HashMap<String, String>();

            context.headers().forEach(h -> headers.put(h.key(), new String(h.value())));
            sleep();
            producer.send(k, v, headers);
            context.commit();
        } catch (ExecutionException e) {
            Tags.ERROR.set(span, true);
            span.log(Map.of(
                    Fields.EVENT, "error",
                    Fields.ERROR_OBJECT, e,
                    Fields.MESSAGE, e.getMessage()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            span.finish();
        }
    }

    @Override
    public void close() {
        // no implementation
    }

    private Span tracingSpan() {
        var spanBuilder = tracer.buildSpan(this.getClass().getName());
        var spanContext = TracingKafkaUtils.extractSpanContext(context.headers(), tracer);

        if (spanContext != null) {
            spanBuilder.asChildOf(spanContext);
        }

        var span = spanBuilder.start();
        context.headers().forEach(h -> span.setTag(h.key(), new String(h.value())));
        return span;
    }

    private void sleep() {
        if (delayMs == 0) {
            return;
        }
        log.info("Sleeping for '{}' ms...", delayMs);
        Awaitility.await().atLeast(delayMs, TimeUnit.MILLISECONDS);
    }
}
